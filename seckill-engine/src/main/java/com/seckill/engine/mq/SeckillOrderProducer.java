package com.seckill.engine.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

/** 秒杀订单 RocketMQ 生产者 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillOrderProducer {

  private static final String TOPIC = "seckill-order-topic";

  private final RocketMQTemplate rocketMQTemplate;

  /**
   * 发送订单消息到 RocketMQ
   *
   * @param message 订单消息
   */
  public void send(OrderMessage message) {
    rocketMQTemplate.syncSend(TOPIC, MessageBuilder.withPayload(message).build());
    log.info("消息发送成功: orderNo={}", message.getOrderNo());
  }
}
