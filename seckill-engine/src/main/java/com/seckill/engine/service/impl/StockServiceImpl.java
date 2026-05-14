package com.seckill.engine.service.impl;

import static com.seckill.common.constant.RedisKeyConstants.*;

import com.seckill.engine.service.StockService;
import com.seckill.framework.exception.ClientException;
import com.seckill.framework.exception.ServiceException;
import com.seckill.framework.toolkit.StockDecrementReturnCombinedUtil;
import jakarta.annotation.PostConstruct;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {
  private final StringRedisTemplate stringRedisTemplate;
  private DefaultRedisScript<Long> deductScript;
  private DefaultRedisScript<Long> compensateScript;

  @PostConstruct
  public void init() {
    deductScript = new DefaultRedisScript<>();
    deductScript.setScriptSource(
        new ResourceScriptSource(new ClassPathResource("lua/stock_deduct.lua")));
    deductScript.setResultType(Long.class);

    compensateScript = new DefaultRedisScript<>();
    compensateScript.setScriptSource(
        new ResourceScriptSource(new ClassPathResource("lua/stock_compensate.lua")));
    compensateScript.setResultType(Long.class);
  }

  @Override
  public long deductStock(Long activityId, Long userId) {
    int bucketCount = getBucketCount(activityId);
    int bucket = (int) (userId % bucketCount);
    String stockKey = stockKey(activityId, bucket);
    String boughtKey = boughtKey(activityId);
    String totalKey = totalKey(activityId);
    List<String> keys = Arrays.asList(stockKey, boughtKey, totalKey);

    Long result =
        stringRedisTemplate.execute(deductScript, keys, String.valueOf(userId));
    if (result == null) {
      throw new ServiceException("B000100", "库存扣减异常");
    }

    int errorCode = StockDecrementReturnCombinedUtil.parseErrorCode(result);
    long remaining = StockDecrementReturnCombinedUtil.parseCount(result);

    switch (errorCode) {
      case 0:
        verifyAfterDeduct(stockKey, activityId, userId, bucket);
        return remaining;
      case 1:
        // 注意：此处"库存不足"指当前桶已空，总库存可能仍有剩余（分桶策略的已知限制）
        throw new ClientException("A000410", "库存不足");
      case 2:
        throw new ClientException("A000400", "您已购买过");
      default:
        throw new ServiceException("B000100", "未知扣减结果: " + errorCode);
    }
  }

  private void verifyAfterDeduct(
      String stockKey, Long activityId, Long userId, int bucket) {
    String val = stringRedisTemplate.opsForValue().get(stockKey);
    if (val == null || Long.parseLong(val) < 0) {
      log.warn(
          "写后读校验失败，回补库存: activityId={}, userId={}, stockKey={}",
          activityId,
          userId,
          stockKey);
      compensateStock(activityId, userId, bucket);
      throw new ServiceException("B000100", "库存扣减异常，请重试");
    }
  }

  @Override
  public void compensateStock(Long activityId, Long userId, int bucketIndex) {
    String stockKey = stockKey(activityId, bucketIndex);
    String boughtKey = boughtKey(activityId);
    List<String> keys = Arrays.asList(stockKey, boughtKey);
    stringRedisTemplate.execute(compensateScript, keys, String.valueOf(userId));
  }

  @Override
  public void initActivityStock(Long activityId, int totalCount, int bucketCount, LocalDateTime endTime) {
    String totalKey = totalKey(activityId);
    if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(totalKey))) {
      log.warn("库存已初始化，跳过重复初始化: activityId={}", activityId);
      return;
    }

    long ttlSeconds = calculateTtlSeconds(endTime);

    int perBucket = totalCount / bucketCount;
    int remainder = totalCount % bucketCount;
    for (int i = 0; i < bucketCount; i++) {
      int stock = perBucket + (i < remainder ? 1 : 0);
      String key = stockKey(activityId, i);
      stringRedisTemplate.opsForValue().set(key, String.valueOf(stock));
      if (ttlSeconds > 0) {
        stringRedisTemplate.expire(key, ttlSeconds, java.util.concurrent.TimeUnit.SECONDS);
      }
    }
    stringRedisTemplate.opsForValue().set(totalKey, String.valueOf(totalCount));
    if (ttlSeconds > 0) {
      stringRedisTemplate.expire(totalKey, ttlSeconds, java.util.concurrent.TimeUnit.SECONDS);
    }
    log.info(
        "活动库存初始化完成: activityId={}, total={}, buckets={}, ttl={}s",
        activityId,
        totalCount,
        bucketCount,
        ttlSeconds);
  }

  private long calculateTtlSeconds(LocalDateTime endTime) {
    long bufferSeconds = 2 * 3600;
    return Duration.between(LocalDateTime.now(), endTime).getSeconds() + bufferSeconds;
  }

  private int getBucketCount(Long activityId) {
    Object val = stringRedisTemplate.opsForHash().get(activityKey(activityId), "bucketCount");
    if (val == null) {
      throw new ServiceException("B000100", "活动缓存未初始化，无法扣减库存: activityId=" + activityId);
    }
    return Integer.parseInt((String) val);
  }

  @Override
  public Long getTotalStock(Long activityId) {
    String val = stringRedisTemplate.opsForValue().get(totalKey(activityId));
    return val != null ? Long.parseLong(val) : 0L;
  }
}
