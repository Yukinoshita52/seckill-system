package com.seckill.engine.mq.consumer;

import static com.seckill.engine.common.constant.RedisKeyConstants.*;

import com.seckill.engine.controller.DLQTestController;
import com.seckill.engine.dao.entity.StockChangeLogDO;
import com.seckill.engine.mq.OrderMessage;
import com.seckill.engine.service.StockChangeLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

/** 秒杀订单 RocketMQ 消费者 — 记录库存扣减变化 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
    topic = "seckill-order-topic",
    consumerGroup = "seckill-consumer-group",
    maxReconsumeTimes = 3,
    consumeTimeout = 15000
)
public class SeckillOrderConsumer implements RocketMQListener<OrderMessage> {

  private final StockChangeLogService stockChangeLogService;

  @Override
  public void onMessage(OrderMessage message) {
    log.info("收到消息: orderNo={}, userId={}, activityId={}", message.getOrderNo(), message.getUserId(), message.getActivityId());

    // DLQ 测试开关：开启时模拟消费失败，消息将重试并进入死信队列
    if (DLQTestController.FORCE_FAIL.get()) {
      log.warn("模拟消费失败（DLQ测试开关已开启）: orderNo={}", message.getOrderNo());
      throw new RuntimeException("模拟消费失败，触发 DLQ");
    }

    try {
      stockChangeLogService.log(StockChangeLogDO.builder()
              .activityId(message.getActivityId())
              .userId(message.getUserId())
              .orderNo(message.getOrderNo())
              .changeType(0)
              .changeQuantity(1)
              .bucketIndex(message.getBucketIndex())
              .build());
    } catch (DuplicateKeyException e) {
      // 幂等：orderNo 已存在，说明是重复消息，直接跳过不算消费失败
      log.warn("orderNo 已存在，跳过重复消费: orderNo={}", message.getOrderNo());
    }
  }
}