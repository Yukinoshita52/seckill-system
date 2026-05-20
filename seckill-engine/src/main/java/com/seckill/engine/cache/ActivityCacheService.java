package com.seckill.engine.cache;

import static com.seckill.common.constant.RedisKeyConstants.ACTIVITY_PREFIX;

import com.seckill.common.dao.entity.SeckillActivityDO;
import com.seckill.common.dao.mapper.SeckillActivityMapper;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** 活动信息 Redis 缓存读写层（含穿透/击穿防护） */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityCacheService {

  private static final String KEY_PREFIX = ACTIVITY_PREFIX;
  private static final String NULL_MARKER = "_null";
  private static final long NULL_TTL_MINUTES = 2;
  private static final long LOCK_TTL_SECONDS = 5;
  private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final StringRedisTemplate stringRedisTemplate;
  private final SeckillActivityMapper activityMapper;

  // todo: 后续用 xxl-job 定时调用，实现活动开始前自动预热缓存
  /** 从 DB 加载活动信息并写入 Redis，用于活动初始化阶段 */
  public void initActivityCache(Long activityId) {
    SeckillActivityDO activity = activityMapper.selectById(activityId);
    if (activity == null) {
      log.warn("活动不存在，跳过缓存初始化: activityId={}", activityId);
      return;
    }
    syncToRedis(activity);
  }

  /** 将活动信息写入 Redis Hash */
  public void syncToRedis(SeckillActivityDO activity) {
    String key = KEY_PREFIX + activity.getId();
    Map<String, String> hash = new HashMap<>();
    hash.put("id", String.valueOf(activity.getId()));
    hash.put("activityName", nullToEmpty(activity.getActivityName()));
    hash.put("goodsId", String.valueOf(activity.getGoodsId()));
    hash.put("goodsName", nullToEmpty(activity.getGoodsName()));
    hash.put("originalPrice", activity.getOriginalPrice().toPlainString());
    hash.put("seckillPrice", activity.getSeckillPrice().toPlainString());
    hash.put("totalStock", String.valueOf(activity.getTotalStock()));
    hash.put("bucketCount", String.valueOf(activity.getBucketCount()));
    hash.put("startTime", activity.getStartTime().format(FMT));
    hash.put("endTime", activity.getEndTime().format(FMT));
    hash.put("status", String.valueOf(activity.getStatus()));

    stringRedisTemplate.opsForHash().putAll(key, hash);

    // TTL = 活动结束时间 + 2小时
    long ttlSeconds = TimeUnit.HOURS.toSeconds(2)
        + java.time.Duration.between(LocalDateTime.now(), activity.getEndTime()).getSeconds();
    if (ttlSeconds > 0) {
      stringRedisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
    }

    log.debug("活动缓存已写入: key={}", key);
  }

  /**
   * 双检锁读缓存，防穿透 + 防击穿。
   *
   * <p>穿透防护：DB 不存在时写入空标记（TTL 2分钟），后续请求不再查 DB。
   * <p>击穿防护：双检锁模式，保证只有一个线程查 DB。
   * <pre>
   * 第一次读缓存 → miss → 尝试加锁
   *   → 拿到锁 → 第二次读缓存 → miss → 查 DB → 写缓存 → 释放锁
   *   → 拿到锁 → 第二次读缓存 → hit → 直接返回（其他线程已加载）
   *   → 未拿到锁 → sleep → 重试（回到第一次读缓存）
   * </pre>
   */
  public SeckillActivityDO getWithProtection(Long activityId) {
    // 1. 第一次读缓存
    SeckillActivityDO cached = getFromRedis(activityId);
    if (cached != null) {
      return cached;
    }

    // 2. 缓存 miss，尝试获取互斥锁
    String lockKey = KEY_PREFIX + "lock:" + activityId;
    Boolean locked = stringRedisTemplate.opsForValue()
        .setIfAbsent(lockKey, "1", LOCK_TTL_SECONDS, TimeUnit.SECONDS);

    if (Boolean.TRUE.equals(locked)) {
      // 拿到锁，双检：第二次读缓存
      try {
        cached = getFromRedis(activityId);
        if (cached != null) {
          return cached;
        }

        // 第二次也没命中，回源 DB
        SeckillActivityDO activity = activityMapper.selectById(activityId);
        if (activity != null) {
          syncToRedis(activity);
          return activity;
        } else {
          // 穿透防护：DB 不存在，写入空标记
          cacheNullMarker(activityId);
          return null;
        }
      } finally {
        stringRedisTemplate.delete(lockKey);
      }
    } else {
      // 未拿到锁，其他线程正在加载，sleep 后重试
      try {
        Thread.sleep(50);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
      }
      return getFromRedis(activityId);
    }
  }

  /** 从 Redis Hash 读取活动信息，返回 null 表示缓存未命中或为空标记 */
  public SeckillActivityDO getFromRedis(Long activityId) {
    String key = KEY_PREFIX + activityId;
    Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
    if (entries.isEmpty()) {
      return null;
    }
    // 穿透防护：命中空标记
    if (entries.containsKey(NULL_MARKER)) {
      return null;
    }
    try {
      return SeckillActivityDO.builder()
          .id(Long.parseLong((String) entries.get("id")))
          .activityName((String) entries.get("activityName"))
          .goodsId(Long.parseLong((String) entries.get("goodsId")))
          .goodsName((String) entries.get("goodsName"))
          .originalPrice(new BigDecimal((String) entries.get("originalPrice")))
          .seckillPrice(new BigDecimal((String) entries.get("seckillPrice")))
          .totalStock(Integer.parseInt((String) entries.get("totalStock")))
          .bucketCount(Integer.parseInt((String) entries.get("bucketCount")))
          .startTime(LocalDateTime.parse((String) entries.get("startTime"), FMT))
          .endTime(LocalDateTime.parse((String) entries.get("endTime"), FMT))
          .status(Integer.parseInt((String) entries.get("status")))
          .build();
    } catch (Exception e) {
      log.warn("活动缓存反序列化失败: key={}, reason={}", key, e.getMessage());
      return null;
    }
  }

  /** 写入空标记，短 TTL 防穿透 */
  private void cacheNullMarker(Long activityId) {
    String key = KEY_PREFIX + activityId;
    Map<String, String> marker = new HashMap<>();
    marker.put(NULL_MARKER, "1");
    stringRedisTemplate.opsForHash().putAll(key, marker);
    stringRedisTemplate.expire(key, NULL_TTL_MINUTES, TimeUnit.MINUTES);
    log.debug("写入空标记: key={}", key);
  }

  /** 删除活动缓存 */
  public void deleteFromRedis(Long activityId) {
    stringRedisTemplate.delete(KEY_PREFIX + activityId);
  }

  private String nullToEmpty(String s) {
    return s != null ? s : "";
  }
}
