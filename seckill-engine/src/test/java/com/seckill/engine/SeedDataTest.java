package com.seckill.engine;

import com.seckill.common.dao.entity.SeckillActivityDO;
import com.seckill.common.dao.mapper.SeckillActivityMapper;
import com.seckill.engine.service.StockService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class SeedDataTest {

  @Autowired private SeckillActivityMapper activityMapper;
  @Autowired private StockService stockService;

  @Test
  void seedActivity() {
    Long activityId = 1L;

    // 先查是否已存在
    SeckillActivityDO existing = activityMapper.selectById(activityId);
    if (existing != null) {
      log.info("活动已存在: {}", existing);
    } else {
      SeckillActivityDO activity =
          SeckillActivityDO.builder()
              .id(activityId)
              .activityName("iPhone 16 限时秒杀")
              .goodsId(1001L)
              .goodsName("Apple iPhone 16 256G 黑色")
              .originalPrice(new BigDecimal("6999.00"))
              .seckillPrice(new BigDecimal("4999.00"))
              .totalStock(1000)
              .bucketCount(5)
              .startTime(LocalDateTime.now().minusHours(1))
              .endTime(LocalDateTime.now().plusHours(2))
              .status(1)
              .build();
      activityMapper.insert(activity);
      log.info("活动插入成功: id={}", activityId);
    }

    // 初始化 Redis 库存
    stockService.initActivityStock(activityId, 1000, 5, LocalDateTime.now().plusHours(2));
    log.info("Redis 库存初始化完成: activityId={}, stock=1000, buckets=5");
  }

  @Test
  void seedActivity2() {
    Long activityId = 2L;

    SeckillActivityDO existing = activityMapper.selectById(activityId);
    if (existing != null) {
      log.info("活动已存在: {}", existing);
    } else {
      SeckillActivityDO activity =
          SeckillActivityDO.builder()
              .id(activityId)
              .activityName("AirPods Pro 2 限时特价")
              .goodsId(1002L)
              .goodsName("Apple AirPods Pro 2 USB-C")
              .originalPrice(new BigDecimal("1899.00"))
              .seckillPrice(new BigDecimal("1299.00"))
              .totalStock(500)
              .bucketCount(5)
              .startTime(LocalDateTime.now().plusHours(1))
              .endTime(LocalDateTime.now().plusHours(4))
              .status(0)
              .build();
      activityMapper.insert(activity);
      log.info("活动插入成功: id={}", activityId);
    }

    stockService.initActivityStock(activityId, 500, 5, LocalDateTime.now().plusHours(4));
    log.info("Redis 库存初始化完成: activityId={}, stock=500, buckets=5");
  }
}
