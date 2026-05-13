package com.seckill.engine.service.chain;

import com.seckill.framework.exception.ClientException;
import com.seckill.framework.toolkit.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** 用户重复购买校验处理器 — 检查 Redis bought set */
@Component
@RequiredArgsConstructor
public class UserRepeatCheckHandler implements SeckillChainHandler {

  private final StringRedisTemplate stringRedisTemplate;

  @Override
  public void handle(SeckillChainContext context) {
    Long activityId = context.getReq().getActivityId();
    Long userId = UserContext.getUserId();
    String boughtKey = "bought:" + activityId;

    Boolean isMember = stringRedisTemplate.opsForSet().isMember(boughtKey, String.valueOf(userId));
    if (Boolean.TRUE.equals(isMember)) {
      throw new ClientException("A000400", "您已购买过，不能重复购买");
    }
  }

  @Override
  public String name() {
    return "UserRepeatCheck";
  }
}
