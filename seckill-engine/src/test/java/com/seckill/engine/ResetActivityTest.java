package com.seckill.engine;

import com.seckill.engine.dao.entity.SeckillActivityDO;
import com.seckill.engine.dao.entity.SeckillOrderDO;
import com.seckill.engine.dao.entity.StockChangeLogDO;
import com.seckill.engine.dao.mapper.SeckillActivityMapper;
import com.seckill.engine.dao.mapper.SeckillOrderMapper;
import com.seckill.engine.dao.mapper.StockChangeLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

@Slf4j
@SpringBootTest
@RequiredArgsConstructor
public class ResetActivityTest {

  @Autowired private SeckillActivityMapper activityMapper;
  @Autowired private SeckillOrderMapper orderMapper;
  @Autowired private StockChangeLogMapper changeLogMapper;
  @Autowired private StringRedisTemplate redisTemplate;

  @Test
  public void resetActivity(){
    Long activityId = 22L;
    resetActivity(activityId);
  }

  /**
   * 重置指定活动的所有数据：删除订单、删除库存变化日志、还原 Redis 库存
   *
   * @param activityId 活动ID
   */
  @Test
  public void resetActivity(Long activityId) {
    // 1. 查询活动
    SeckillActivityDO activity = activityMapper.selectById(activityId);
    if (activity == null) {
      log.warn("活动不存在: activityId={}", activityId);
      return;
    }
    if (activity.getStatus() == null || activity.getStatus() == 2 || activity.getStatus() == 3) {
      log.warn("活动已结束，不允许重置: activityId={}, status={}", activityId, activity.getStatus());
      return;
    }

    log.info("开始重置活动: activityId={}, totalStock={}, bucketCount={}",
        activityId, activity.getTotalStock(), activity.getBucketCount());

    // 2. 删除 DB 订单
    int orderDeleted = orderMapper.delete(
        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SeckillOrderDO>()
            .eq(SeckillOrderDO::getActivityId, activityId));
    log.info("删除订单数: {}", orderDeleted);

    // 3. 删除库存变化日志
    int logDeleted = changeLogMapper.delete(
        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StockChangeLogDO>()
            .eq(StockChangeLogDO::getActivityId, activityId));
    log.info("删除库存日志数: {}", logDeleted);

    // 4. 还原 Redis 分桶库存
    int totalStock = activity.getTotalStock();
    int bucketCount = activity.getBucketCount();
    int perBucket = totalStock / bucketCount;
    int remainder = totalStock % bucketCount;

    for (int i = 0; i < bucketCount; i++) {
      int bucketStock = perBucket + (i < remainder ? 1 : 0);
      String bucketKey = "stock:" + activityId + ":" + i;
      redisTemplate.opsForValue().set(bucketKey, String.valueOf(bucketStock));
      log.debug("还原分桶库存: key={}, value={}", bucketKey, bucketStock);
    }
    log.info("还原分桶库存完成: bucketCount={}", bucketCount);

    // 5. 还原 Redis 总库存
    String totalKey = "total:" + activityId;
    redisTemplate.opsForValue().set(totalKey, String.valueOf(totalStock));
    log.info("还原总库存: key={}, value={}", totalKey, totalStock);

    // 6. 删除 Redis 已购买用户 Set
    String boughtKey = "bought:" + activityId;
    redisTemplate.delete(boughtKey);
    log.info("删除已购买用户Set: key={}", boughtKey);

    // 7. 还原 Redis activity hash 中的 status 为 1（进行中）
    String activityKey = "activity:" + activityId;
    redisTemplate.opsForHash().put(activityKey, "status", "1");
    log.info("还原活动状态: key={}, status=1", activityKey);

    log.info("活动重置完成: activityId={}", activityId);
  }
}