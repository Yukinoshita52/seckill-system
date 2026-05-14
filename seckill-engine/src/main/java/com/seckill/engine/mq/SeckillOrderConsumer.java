package com.seckill.engine.mq;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seckill.common.dao.entity.SeckillOrderDO;
import com.seckill.common.dao.mapper.SeckillOrderMapper;
import com.seckill.engine.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** 秒杀订单 RocketMQ 消费者 — 扣减库存 + 更新订单状态 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = "seckill-order-topic", consumerGroup = "seckill-consumer-group")
public class SeckillOrderConsumer implements RocketMQListener<OrderMessage> {

  private final StockService stockService;
  private final SeckillOrderMapper orderMapper;

  @Override
  @Transactional
  public void onMessage(OrderMessage message) {
    log.info("收到消息: orderNo={}, userId={}, activityId={}",
        message.getOrderNo(), message.getUserId(), message.getActivityId());

    try {
      // 1. 扣减库存（Lua 原子操作，含去重校验）
      long remaining = stockService.deductStock(message.getActivityId(), message.getUserId());

      // 2. 更新订单状态为成功
      orderMapper.update(null, new LambdaUpdateWrapper<SeckillOrderDO>()
          .eq(SeckillOrderDO::getOrderNo, message.getOrderNo())
          .set(SeckillOrderDO::getStatus, 1));

      log.info("订单处理成功: orderNo={}, remaining={}", message.getOrderNo(), remaining);

    } catch (Exception e) {
      log.error("订单处理失败: orderNo={}, reason={}", message.getOrderNo(), e.getMessage());

      // 3. 更新订单状态为失败
      orderMapper.update(null, new LambdaUpdateWrapper<SeckillOrderDO>()
          .eq(SeckillOrderDO::getOrderNo, message.getOrderNo())
          .set(SeckillOrderDO::getStatus, 2));
    }
  }
}
