package com.seckill.engine.mq.consumer;

import com.seckill.engine.dao.entity.StockChangeLogDO;
import com.seckill.engine.dao.mapper.StockChangeLogMapper;
import com.seckill.engine.mq.OrderMessage;
import com.seckill.engine.service.StockChangeLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

/** 秒杀订单消费者 DLQ — 兜底处理重试耗尽的消息 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
    topic = "%DLQ%seckill-consumer-group",
    consumerGroup = "seckill-consumer-group-dlq"
)
public class SeckillOrderDLQConsumer implements RocketMQListener<OrderMessage> {

  private final StockChangeLogService stockChangeLogService;
  private final StockChangeLogMapper stockChangeLogMapper;

  @Override
  public void onMessage(OrderMessage message) {
    log.warn("DLQ 收到消息: orderNo={}, userId={}, activityId={}",
        message.getOrderNo(), message.getUserId(), message.getActivityId());

    // 幂等保护：orderNo 已存在于 change_log，说明主消费者已处理成功，此处跳过
    boolean exists = stockChangeLogMapper.selectCount(
        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StockChangeLogDO>()
            .eq(StockChangeLogDO::getOrderNo, message.getOrderNo())) > 0;
    if (exists) {
      log.info("DLQ 检测到 orderNo 已存在，跳过: orderNo={}", message.getOrderNo());
      return;
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
      log.info("DLQ 补偿成功: orderNo={}", message.getOrderNo());
    } catch (Exception e) {
      log.error("DLQ 补偿失败: orderNo={}, 请人工介入", message.getOrderNo(), e);
      // TODO: 接入告警通知（钉钉/飞书）
    }
  }
}