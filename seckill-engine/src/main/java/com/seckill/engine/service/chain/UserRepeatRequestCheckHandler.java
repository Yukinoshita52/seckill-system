package com.seckill.engine.service.chain;

import static com.seckill.common.constant.RedisKeyConstants.requestKey;

import com.seckill.framework.exception.ClientException;
import com.seckill.framework.toolkit.UserContext;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** 用户重复抢购请求校验处理器 — 检查 Redis request set，防止并发重复请求穿透 */
@Component
@RequiredArgsConstructor
public class UserRepeatRequestCheckHandler implements SeckillChainHandler {

  private final StringRedisTemplate stringRedisTemplate;

  @Override
  public void handle(SeckillChainContext context) {
    Long activityId = context.getReq().getActivityId();
    Long userId = UserContext.getUserId();
    String key = requestKey(activityId, userId);

    Boolean success = stringRedisTemplate.opsForValue()
        .setIfAbsent(key, "1", Duration.ofSeconds(30));
    if (!Boolean.TRUE.equals(success)) {
      throw new ClientException("A000401", "请求过于频繁，请稍后重试");
    }
  }

  @Override
  public String name() {
    return "UserRepeatRequestCheck";
  }
}