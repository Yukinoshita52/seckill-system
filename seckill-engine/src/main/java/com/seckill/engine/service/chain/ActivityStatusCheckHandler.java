package com.seckill.engine.service.chain;

import com.seckill.common.dao.entity.SeckillActivityDO;
import com.seckill.common.dao.mapper.SeckillActivityMapper;
import com.seckill.engine.service.impl.ActivityCacheService;
import com.seckill.framework.exception.ClientException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 活动状态校验处理器 — 优先读 Redis 缓存，miss 时 fallback 到 DB */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityStatusCheckHandler implements SeckillChainHandler {

  private final ActivityCacheService activityCacheService;
  private final SeckillActivityMapper activityMapper;

  @Override
  public void handle(SeckillChainContext context) {
    Long activityId = context.getReq().getActivityId();

    // 优先从 Redis 读取
    SeckillActivityDO activity = activityCacheService.getFromRedis(activityId);

    // 缓存未命中时 fallback 到 DB
    // todo: 此处可能存在缓存穿透（不存在的 activityId）和缓存击穿（热点活动缓存过期瞬间大量请求击穿到 DB），需加防护
    if (activity == null) {
      log.warn("活动缓存未命中，fallback 到 DB: activityId={}", activityId);
      activity = activityMapper.selectById(activityId);
    }

    if (activity == null) {
      throw new ClientException("A000100", "活动不存在");
    }

    LocalDateTime now = LocalDateTime.now();
    if (now.isBefore(activity.getStartTime())) {
      throw new ClientException("A000200", "活动未开始");
    }
    if (now.isAfter(activity.getEndTime())) {
      throw new ClientException("A000300", "活动已结束");
    }

    context.setActivity(activity);
  }

  @Override
  public String name() {
    return "ActivityStatusCheck";
  }
}
