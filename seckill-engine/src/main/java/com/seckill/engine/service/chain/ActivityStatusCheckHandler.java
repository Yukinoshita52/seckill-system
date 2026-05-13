package com.seckill.engine.service.chain;

import com.seckill.engine.dao.entity.SeckillActivityDO;
import com.seckill.engine.dao.mapper.SeckillActivityMapper;
import com.seckill.framework.exception.ClientException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** 活动状态校验处理器 — 查 DB 并缓存到上下文 */
@Component
@RequiredArgsConstructor
public class ActivityStatusCheckHandler implements SeckillChainHandler {

  private final SeckillActivityMapper activityMapper;

  @Override
  public void handle(SeckillChainContext context) {
    SeckillActivityDO activity = activityMapper.selectById(context.getReq().getActivityId());
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
