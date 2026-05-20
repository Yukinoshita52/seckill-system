package com.seckill.engine.mq.consumer;

import static com.seckill.engine.common.constant.RedisKeyConstants.orderStatusKey;
import static com.seckill.engine.common.constant.RedisKeyConstants.requestKey;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seckill.engine.dao.entity.SeckillOrderDO;
import com.seckill.engine.dao.entity.StockDeductLogDO;
import com.seckill.engine.dao.mapper.SeckillOrderMapper;
import com.seckill.engine.dao.mapper.StockDeductLogMapper;
import com.seckill.engine.dao.entity.SeckillActivityDO;
import com.seckill.engine.dao.mapper.SeckillActivityMapper;
import com.seckill.engine.mq.OrderMessage;
import com.seckill.engine.service.StockService;
import com.alibaba.fastjson2.JSON;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.data.redis.core.StringRedisTemplate;
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
  private final SeckillActivityMapper activityMapper;
  private final StockDeductLogMapper deductLogMapper;
  private final StringRedisTemplate stringRedisTemplate;

  private String mapStatus(Integer status) {
    return switch (status) {
      case 0 -> "PENDING";
      case 1 -> "UNPAID";
      case 2 -> "SUCCESS";
      case 3 -> "FAILED";
      case 4 -> "TIMEOUT";
      default -> "UNKNOWN";
    };
  }

  private void cacheOrderStatus(String orderNo, Integer status, Long activityId, java.math.BigDecimal seckillPrice) {
    String goodsName = "";
    if (activityId != null) {
      SeckillActivityDO activity = activityMapper.selectById(activityId);
      if (activity != null) {
        goodsName = activity.getGoodsName();
      }
    }
    com.seckill.engine.dto.resp.OrderStatusRespDTO dto = com.seckill.engine.dto.resp.OrderStatusRespDTO.builder()
        .orderNo(orderNo)
        .status(mapStatus(status))
        .seckillPrice(seckillPrice)
        .goodsName(goodsName)
        .build();
    stringRedisTemplate.opsForValue().set(orderStatusKey(orderNo), JSON.toJSONString(dto), java.time.Duration.ofMinutes(10));
  }

  @Override
  @Transactional
  public void onMessage(OrderMessage message) {
    log.info("收到消息: orderNo={}, userId={}, activityId={}",
        message.getOrderNo(), message.getUserId(), message.getActivityId());

    try {
      // 1. 扣减库存（Lua 原子操作，含去重校验）
      long remaining = stockService.deductStock(message.getActivityId(), message.getUserId());

      // 2. 写入库存扣减审计日志
      deductLogMapper.insert(StockDeductLogDO.builder()
          .activityId(message.getActivityId())
          .userId(message.getUserId())
          .bucketIndex(message.getBucketIndex())
          .build());

      // 3. 更新订单状态为 UNPAID（库存已扣，待支付）
      orderMapper.update(null, new LambdaUpdateWrapper<SeckillOrderDO>()
          .eq(SeckillOrderDO::getOrderNo, message.getOrderNo())
          .set(SeckillOrderDO::getStatus, 1));

      // 4. 写入订单状态缓存
      cacheOrderStatus(message.getOrderNo(), 1, message.getActivityId(), message.getSeckillPrice());

      log.info("订单处理成功，待支付: orderNo={}, remaining={}", message.getOrderNo(), remaining);

    } catch (Exception e) {
      log.error("订单处理失败: orderNo={}, reason={}", message.getOrderNo(), e.getMessage());

      // 删除 request key，允许用户重试
      stringRedisTemplate.delete(requestKey(message.getActivityId(), message.getUserId()));

      // 更新订单状态为 FAILED
      orderMapper.update(null, new LambdaUpdateWrapper<SeckillOrderDO>()
          .eq(SeckillOrderDO::getOrderNo, message.getOrderNo())
          .set(SeckillOrderDO::getStatus, 3));

      // 写入订单状态缓存
      cacheOrderStatus(message.getOrderNo(), 3, message.getActivityId(), message.getSeckillPrice());
    }
  }
}
