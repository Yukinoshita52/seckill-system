package com.seckill.engine.service.impl;

import com.seckill.engine.service.StockService;
import com.seckill.framework.exception.ClientException;
import com.seckill.framework.exception.ServiceException;
import com.seckill.framework.toolkit.StockDecrementReturnCombinedUtil;
import jakarta.annotation.PostConstruct;
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
    // todo: 桶数量应从活动配置读取（通过参数传入或查 Redis），当前硬编码为 5
    // todo: Redis key 模式应抽取为常量类或工具方法，统一管理 key 前缀和格式
    int bucket = (int) (userId % 5);
    String stockKey = "stock:" + activityId + ":" + bucket;
    String boughtKey = "bought:" + activityId;
    String totalKey = "total:" + activityId;
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

  // todo: 评估写后读校验的必要性和性能开销；若 Lua 脚本已保证正确性，可考虑移除
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
    String stockKey = "stock:" + activityId + ":" + bucketIndex;
    String boughtKey = "bought:" + activityId;
    List<String> keys = Arrays.asList(stockKey, boughtKey);
    stringRedisTemplate.execute(compensateScript, keys, String.valueOf(userId));
  }

  // todo: 为 stock/bought/total key 设置 TTL（活动结束时间 + buffer），活动结束后自动清理
  // todo: 添加幂等校验（如检查 key 是否已存在），防止重复初始化覆盖正在使用的库存
  @Override
  public void initActivityStock(Long activityId, int totalCount, int bucketCount) {
    int perBucket = totalCount / bucketCount;
    int remainder = totalCount % bucketCount;
    for (int i = 0; i < bucketCount; i++) {
      int stock = perBucket + (i < remainder ? 1 : 0);
      stringRedisTemplate
          .opsForValue()
          .set("stock:" + activityId + ":" + i, String.valueOf(stock));
    }
    stringRedisTemplate
        .opsForValue()
        .set("total:" + activityId, String.valueOf(totalCount));
    log.info(
        "活动库存初始化完成: activityId={}, total={}, buckets={}",
        activityId,
        totalCount,
        bucketCount);
  }

  @Override
  public Long getTotalStock(Long activityId) {
    String val = stringRedisTemplate.opsForValue().get("total:" + activityId);
    return val != null ? Long.parseLong(val) : 0L;
  }
}
