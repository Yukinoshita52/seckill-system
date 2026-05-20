package com.seckill.engine.service.chain;

import com.seckill.common.dao.entity.SeckillActivityDO;
import com.seckill.engine.cache.ActivityCacheService;
import com.seckill.framework.exception.ClientException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 活动状态校验处理器 — 读 Redis 缓存（含穿透/击穿防护），miss 时回源 DB */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityStatusCheckHandler implements SeckillChainHandler {

  private final ActivityCacheService activityCacheService;

  @Override
  public void handle(SeckillChainContext context) {
    Long activityId = context.getReq().getActivityId();

    SeckillActivityDO activity = activityCacheService.getWithProtection(activityId);

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
