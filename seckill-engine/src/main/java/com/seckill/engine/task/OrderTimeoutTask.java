package com.seckill.engine.task;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seckill.engine.dao.entity.StockChangeLogDO;
import com.seckill.engine.dao.entity.SeckillOrderDO;
import com.seckill.engine.dao.mapper.SeckillOrderMapper;
import com.seckill.engine.service.StockChangeLogService;
import com.seckill.engine.service.StockService;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 订单超时关单定时任务。
 *
 * <p>扫描 UNPAID 状态（库存已扣，待支付）的超时订单 → CAS 标记 TIMEOUT 并回补库存。
 *
 * <p>UNPAID 关单与支付回调互斥：两者都基于 status=UNPAID 做 CAS，只有一个能成功。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

  private final SeckillOrderMapper orderMapper;
  private final StockService stockService;
  private final StockChangeLogService stockChangeLogService;

  @Value("${seckill.order.timeout-minutes:30}")
  private int timeoutMinutes;

  private static final int STATUS_UNPAID = 1;
  private static final int STATUS_TIMEOUT = 4;
  private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  @Scheduled(fixedDelayString = "${seckill.order.timeout-scan-interval:60000}")
  public void closeUnpaidOrders() {
    String beforeTime = LocalDateTime.now().minusMinutes(timeoutMinutes).format(FMT);
    List<SeckillOrderDO> orders =
        orderMapper.selectByStatusAndCreateTime(STATUS_UNPAID, beforeTime);

    if (orders.isEmpty()) {
      return;
    }

    log.info("发现超时 UNPAID 订单 {} 笔，开始关单", orders.size());

    int successCount = 0;
    int failCount = 0;
    for (SeckillOrderDO order : orders) {
      try {
        int affected = orderMapper.update(
            null,
            new LambdaUpdateWrapper<SeckillOrderDO>()
                .eq(SeckillOrderDO::getId, order.getId())
                .eq(SeckillOrderDO::getStatus, STATUS_UNPAID)
                .set(SeckillOrderDO::getStatus, STATUS_TIMEOUT));

        if (affected > 0) {
          stockService.compensateStock(order.getActivityId(), order.getUserId(), order.getBucketIndex());
          stockChangeLogService.log(StockChangeLogDO.builder()
              .activityId(order.getActivityId())
              .userId(order.getUserId())
              .orderNo(order.getOrderNo())
              .changeType(1)
              .changeQuantity(1)
              .bucketIndex(order.getBucketIndex())
              .build());
          successCount++;
        } else {
          log.debug("订单状态已变更，跳过: orderNo={}", order.getOrderNo());
        }
      } catch (Exception e) {
        failCount++;
        log.error("关单失败: orderNo={}", order.getOrderNo(), e);
      }
    }

    log.info("UNPAID 超时关单完成: 成功={}, 失败={}", successCount, failCount);
  }
}