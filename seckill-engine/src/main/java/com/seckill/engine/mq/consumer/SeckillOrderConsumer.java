package com.seckill.engine.mq.consumer;

import static com.seckill.engine.common.constant.RedisKeyConstants.*;

import com.seckill.engine.dao.entity.StockDeductLogDO;
import com.seckill.engine.dao.mapper.StockDeductLogMapper;
import com.seckill.engine.mq.OrderMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 秒杀订单 RocketMQ 消费者 — 写审计日志 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "seckill-order-topic", consumerGroup = "seckill-consumer-group")
public class SeckillOrderConsumer implements RocketMQListener<OrderMessage> {

  private final StockDeductLogMapper deductLogMapper;

  @Override
  @Transactional
  public void onMessage(OrderMessage message) {
    log.info("收到消息: orderNo={}, userId={}, activityId={}",
        message.getOrderNo(), message.getUserId(), message.getActivityId());

    // 写入库存扣减审计日志
    deductLogMapper.insert(StockDeductLogDO.builder()
        .activityId(message.getActivityId())
        .userId(message.getUserId())
        .bucketIndex(message.getBucketIndex())
        .build());

    log.info("订单确认完成: orderNo={}", message.getOrderNo());
  }
}