package com.seckill.engine.service;

/** 库存服务 — Redis Lua 原子操作 */
public interface StockService {

  /**
   * 扣减库存，含用户去重校验
   *
   * @param activityId 活动ID
   * @param userId 用户ID
   * @return 剩余库存
   */
  long deductStock(Long activityId, Long userId);

  /**
   * 回补库存（扣减失败时调用）
   *
   * @param activityId 活动ID
   * @param userId 用户ID
   * @param bucketIndex 库存桶索引
   */
  void compensateStock(Long activityId, Long userId, int bucketIndex);

  /**
   * 初始化活动库存到 Redis（分桶写入）
   *
   * @param activityId 活动ID
   * @param totalCount 总库存数
   * @param bucketCount 桶数量
   */
  void initActivityStock(Long activityId, int totalCount, int bucketCount);

  /**
   * 查询活动总剩余库存
   *
   * @param activityId 活动ID
   * @return 剩余库存
   */
  Long getTotalStock(Long activityId);
}
