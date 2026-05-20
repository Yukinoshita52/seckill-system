package com.seckill.admin.service.impl;

import static com.seckill.common.constant.RedisKeyConstants.*;

import com.seckill.admin.dto.req.ActivityCreateReqDTO;
import com.seckill.admin.dto.req.ActivityUpdateReqDTO;
import com.seckill.admin.dto.resp.ActivityRespDTO;
import com.seckill.admin.service.ActivityAdminService;
import com.seckill.common.dao.entity.SeckillActivityDO;
import com.seckill.common.dao.mapper.SeckillActivityMapper;
import com.seckill.framework.exception.ClientException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityAdminServiceImpl implements ActivityAdminService {

  private final SeckillActivityMapper activityMapper;
  private final StringRedisTemplate stringRedisTemplate;

  @Override
  public ActivityRespDTO create(ActivityCreateReqDTO req) {
    SeckillActivityDO activity = SeckillActivityDO.builder()
        .activityName(req.getActivityName())
        .goodsId(req.getGoodsId())
        .goodsName(req.getGoodsName())
        .originalPrice(req.getOriginalPrice())
        .seckillPrice(req.getSeckillPrice())
        .totalStock(req.getTotalStock())
        .bucketCount(req.getBucketCount())
        .startTime(req.getStartTime())
        .endTime(req.getEndTime())
        .status(0)
        .build();
    activityMapper.insert(activity);
    log.info("活动创建成功: id={}", activity.getId());
    return toRespDTO(activity);
  }

  @Override
  public ActivityRespDTO update(Long id, ActivityUpdateReqDTO req) {
    SeckillActivityDO activity = activityMapper.selectById(id);
    if (activity == null) {
      throw new ClientException("A000100", "活动不存在");
    }

    if (req.getActivityName() != null) activity.setActivityName(req.getActivityName());
    if (req.getGoodsName() != null) activity.setGoodsName(req.getGoodsName());
    if (req.getOriginalPrice() != null) activity.setOriginalPrice(req.getOriginalPrice());
    if (req.getSeckillPrice() != null) activity.setSeckillPrice(req.getSeckillPrice());
    if (req.getTotalStock() != null) activity.setTotalStock(req.getTotalStock());
    if (req.getBucketCount() != null) activity.setBucketCount(req.getBucketCount());
    if (req.getStartTime() != null) activity.setStartTime(req.getStartTime());
    if (req.getEndTime() != null) activity.setEndTime(req.getEndTime());
    if (req.getStatus() != null) activity.setStatus(req.getStatus());

    activityMapper.updateById(activity);
    log.info("活动更新成功: id={}", id);
    return toRespDTO(activity);
  }

  @Override
  public ActivityRespDTO getById(Long id) {
    SeckillActivityDO activity = activityMapper.selectById(id);
    if (activity == null) {
      throw new ClientException("A000100", "活动不存在");
    }
    return toRespDTO(activity);
  }

  @Override
  public List<ActivityRespDTO> list() {
    return activityMapper.selectList(null).stream()
        .map(this::toRespDTO)
        .collect(Collectors.toList());
  }

  @Override
  public void initCache(Long id) {
    SeckillActivityDO activity = activityMapper.selectById(id);
    if (activity == null) {
      throw new ClientException("A000100", "活动不存在");
    }

    String totalKey = totalKey(id);
    if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(totalKey))) {
      log.warn("缓存已初始化，跳过重复初始化: activityId={}", id);
      return;
    }

    // 写入活动信息缓存（activity:{id} Hash）
    java.util.Map<String, String> hash = new java.util.HashMap<>();
    hash.put("id", String.valueOf(activity.getId()));
    hash.put("activityName", activity.getActivityName() != null ? activity.getActivityName() : "");
    hash.put("goodsId", String.valueOf(activity.getGoodsId()));
    hash.put("goodsName", activity.getGoodsName() != null ? activity.getGoodsName() : "");
    hash.put("originalPrice", activity.getOriginalPrice().toPlainString());
    hash.put("seckillPrice", activity.getSeckillPrice().toPlainString());
    hash.put("totalStock", String.valueOf(activity.getTotalStock()));
    hash.put("bucketCount", String.valueOf(activity.getBucketCount()));
    hash.put("startTime", activity.getStartTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    hash.put("endTime", activity.getEndTime().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    hash.put("status", String.valueOf(activity.getStatus()));

    String key = activityKey(id);
    stringRedisTemplate.opsForHash().putAll(key, hash);

    // TTL = 活动结束时间 + 2小时
    long ttlSeconds = java.util.concurrent.TimeUnit.HOURS.toSeconds(2)
        + java.time.Duration.between(java.time.LocalDateTime.now(), activity.getEndTime()).getSeconds();
    if (ttlSeconds > 0) {
      stringRedisTemplate.expire(key, ttlSeconds, java.util.concurrent.TimeUnit.SECONDS);
    }

    // 写入库存缓存（stock:{activityId}:{bucket}），与 engine Lua 脚本 key 一致
    for (int i = 0; i < activity.getBucketCount(); i++) {
      String bucketKey = stockKey(id, i);
      int bucketStock = activity.getTotalStock() / activity.getBucketCount();
      if (i < activity.getTotalStock() % activity.getBucketCount()) {
        bucketStock++;
      }
      stringRedisTemplate.opsForValue().set(bucketKey, String.valueOf(bucketStock));
      if (ttlSeconds > 0) {
        stringRedisTemplate.expire(bucketKey, ttlSeconds, java.util.concurrent.TimeUnit.SECONDS);
      }
    }
    stringRedisTemplate.opsForValue().set(totalKey, String.valueOf(activity.getTotalStock()));
    if (ttlSeconds > 0) {
      stringRedisTemplate.expire(totalKey, ttlSeconds, java.util.concurrent.TimeUnit.SECONDS);
    }

    log.info("活动缓存初始化完成: activityId={}, stock={}, buckets={}, ttl={}s", id, activity.getTotalStock(), activity.getBucketCount(), ttlSeconds);
  }

  private ActivityRespDTO toRespDTO(SeckillActivityDO activity) {
    return ActivityRespDTO.builder()
        .id(activity.getId())
        .activityName(activity.getActivityName())
        .goodsId(activity.getGoodsId())
        .goodsName(activity.getGoodsName())
        .originalPrice(activity.getOriginalPrice())
        .seckillPrice(activity.getSeckillPrice())
        .totalStock(activity.getTotalStock())
        .bucketCount(activity.getBucketCount())
        .startTime(activity.getStartTime())
        .endTime(activity.getEndTime())
        .status(activity.getStatus())
        .createTime(activity.getCreateTime())
        .updateTime(activity.getUpdateTime())
        .build();
  }
}
