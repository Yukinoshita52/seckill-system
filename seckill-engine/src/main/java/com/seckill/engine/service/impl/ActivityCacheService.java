package com.seckill.engine.service.impl;

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

/** 活动信息 Redis 缓存读写层 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityCacheService {

  private static final String KEY_PREFIX = "activity:";
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

    // TTL = 活动结束时间 + 1小时
    long ttlSeconds = TimeUnit.HOURS.toSeconds(1)
        + java.time.Duration.between(LocalDateTime.now(), activity.getEndTime()).getSeconds();
    if (ttlSeconds > 0) {
      stringRedisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
    }

    log.debug("活动缓存已写入: key={}", key);
  }

  /** 从 Redis Hash 读取活动信息，返回 null 表示缓存未命中 */
  public SeckillActivityDO getFromRedis(Long activityId) {
    String key = KEY_PREFIX + activityId;
    Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(key);
    if (entries.isEmpty()) {
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

  /** 删除活动缓存 */
  public void deleteFromRedis(Long activityId) {
    stringRedisTemplate.delete(KEY_PREFIX + activityId);
  }

  private String nullToEmpty(String s) {
    return s != null ? s : "";
  }
}
