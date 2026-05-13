package com.seckill.engine.service.chain;

import com.seckill.framework.exception.ClientException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** 库存预检处理器 — 快速判断总库存是否充足 */
@Component
@RequiredArgsConstructor
public class StockPreCheckHandler implements SeckillChainHandler {

  private final StringRedisTemplate stringRedisTemplate;

  @Override
  public void handle(SeckillChainContext context) {
    Long activityId = context.getReq().getActivityId();
    String totalKey = "total:" + activityId;

    String val = stringRedisTemplate.opsForValue().get(totalKey);
    if (val == null || Long.parseLong(val) <= 0) {
      throw new ClientException("A000410", "库存不足");
    }
  }

  @Override
  public String name() {
    return "StockPreCheck";
  }
}
