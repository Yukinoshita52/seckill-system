package com.seckill.engine.job;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seckill.common.dao.entity.SeckillOrderDO;
import com.seckill.common.dao.mapper.SeckillOrderMapper;
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
 * <p>1. 扫描 PENDING 状态（MQ 未消费，库存未扣减）的超时订单 → 标记 FAILED，无需回补。
 * <p>2. 扫描 UNPAID 状态（库存已扣，待支付）的超时订单 → CAS 标记 TIMEOUT 并回补库存。
 *
 * <p>UNPAID 关单与支付回调互斥：两者都基于 status=UNPAID 做 CAS，只有一个能成功。
 *
 * <p>后续可迁移到 xxl-job，支持分片查询 + 多实例并行处理。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutTask {

  private final SeckillOrderMapper orderMapper;
  private final StockService stockService;

  @Value("${seckill.order.timeout-minutes:15}")
  private int timeoutMinutes;

  private static final int STATUS_PENDING = 0;
  private static final int STATUS_UNPAID = 1;
  private static final int STATUS_FAILED = 3;
  private static final int STATUS_TIMEOUT = 4;
  private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  /** 处理 PENDING 超时：MQ 消息丢失或消费者异常，库存未扣减，直接标记 FAILED */
  @Scheduled(fixedDelayString = "${seckill.order.timeout-scan-interval:60000}")
  public void closePendingOrders() {
    String beforeTime = LocalDateTime.now().minusMinutes(timeoutMinutes).format(FMT);
    List<SeckillOrderDO> orders =
        orderMapper.selectByStatusAndCreateTime(STATUS_PENDING, beforeTime);

    if (orders.isEmpty()) {
      return;
    }

    log.info("发现超时 PENDING 订单 {} 笔，标记为 FAILED", orders.size());

    int successCount = 0;
    for (SeckillOrderDO order : orders) {
      int affected = orderMapper.update(
          null,
          new LambdaUpdateWrapper<SeckillOrderDO>()
              .eq(SeckillOrderDO::getId, order.getId())
              .eq(SeckillOrderDO::getStatus, STATUS_PENDING)
              .set(SeckillOrderDO::getStatus, STATUS_FAILED));
      if (affected > 0) {
        successCount++;
      }
    }

    log.info("PENDING 超时处理完成: 成功={}", successCount);
  }

  /** 处理 UNPAID 超时：库存已扣减，需回补库存，CAS 与支付回调互斥 */
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
