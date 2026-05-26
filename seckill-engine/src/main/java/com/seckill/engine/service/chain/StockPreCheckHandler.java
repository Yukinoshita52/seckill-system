package com.seckill.engine.service.chain;

import static com.seckill.engine.common.constant.RedisKeyConstants.totalKey;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.seckill.framework.exception.ClientException;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** 库存预检处理器 — L1 本地缓存 → L2 Redis 快速判断库存是否充足 */
@Component
@RequiredArgsConstructor
public class StockPreCheckHandler implements SeckillChainHandler {

  private final StringRedisTemplate stringRedisTemplate;

  /** L1 本地库存缓存：3s 短 TTL，库存 > 0 时拦截热点 Redis 查询，库存 = 0 时快速拒绝 */
  private final Cache<Long, Long> stockCache = Caffeine.newBuilder()
      .expireAfterWrite(Duration.ofSeconds(3))
      .maximumSize(500)
      .build();

  @Override
  public void handle(SeckillChainContext context) {
    Long activityId = context.getReq().getActivityId();

    // L1: Caffeine 本地缓存
    Long localStock = stockCache.getIfPresent(activityId);
    if (localStock != null) {
      if (localStock <= 0) {
        throw new ClientException("A000410", "库存不足");
      }
      return;
    }

    // L2: Redis 查询
    String totalKey = totalKey(activityId);
    String val = stringRedisTemplate.opsForValue().get(totalKey);
    long stock = (val != null) ? Long.parseLong(val) : 0L;
    stockCache.put(activityId, stock);

    if (stock <= 0) {
      throw new ClientException("A000410", "库存不足");
    }
  }

  @Override
  public String name() {
    return "StockPreCheck";
  }
}