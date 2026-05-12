package com.seckill.engine.service;

public interface StockService {
  long deductStock(Long activityId, Long userId);

  void compensateStock(Long activityId, Long userId, int bucketIndex);

  void initActivityStock(Long activityId, int totalCount, int bucketCount);

  Long getTotalStock(Long activityId);
}
