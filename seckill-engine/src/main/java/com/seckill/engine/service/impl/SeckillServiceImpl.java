package com.seckill.engine.service.impl;

import com.seckill.engine.dao.entity.SeckillActivityDO;
import com.seckill.engine.dao.entity.SeckillOrderDO;
import com.seckill.engine.dao.mapper.SeckillOrderMapper;
import com.seckill.engine.dto.req.SeckillOrderReqDTO;
import com.seckill.engine.dto.resp.SeckillOrderRespDTO;
import com.seckill.engine.mq.OrderMessage;
import com.seckill.engine.mq.producer.SeckillOrderProducer;
import com.seckill.engine.service.SeckillService;
import com.seckill.engine.service.StockService;
import com.seckill.engine.service.chain.SeckillChainContext;
import com.seckill.engine.service.chain.SeckillChainExecutor;
import com.seckill.framework.exception.ServiceException;
import com.seckill.framework.toolkit.UserContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

  private final SeckillOrderMapper orderMapper;
  private final SeckillChainExecutor chainExecutor;
  private final SeckillOrderProducer orderProducer;
  private final StockService stockService;

  private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  @Override
  public SeckillOrderRespDTO placeOrder(SeckillOrderReqDTO req) {
    Long userId = UserContext.getUserId();

    // 1. 责任链校验（参数 → 活动状态 → 用户去重 → 库存预检）
    SeckillChainContext chainContext = chainExecutor.execute(req);
    SeckillActivityDO activity = chainContext.getActivity();

    // 2. Redis 原子扣库存（Lua脚本，含去重校验）
    int bucket = (int) (userId % activity.getBucketCount());
    stockService.deductStock(activity.getId(), userId);

    // 3. 扣减成功，写入 UNPAID 订单
    String orderNo = generateOrderNo();
    SeckillOrderDO order = SeckillOrderDO.builder()
        .orderNo(orderNo)
        .activityId(activity.getId())
        .userId(userId)
        .seckillPrice(activity.getSeckillPrice())
        .bucketIndex(bucket)
        .status(1)
        .build();
    try {
      orderMapper.insert(order);
    } catch (Exception e){
      log.error("订单写入失败，回补库存: orderNo={}", orderNo, e);
      stockService.compensateStock(activity.getId(), userId, bucket);
      throw e;
    }

    // 4. 异步发送 MQ → 消费者写审计日志 + 缓存订单状态
    OrderMessage message = OrderMessage.builder()
        .orderNo(orderNo)
        .activityId(activity.getId())
        .userId(userId)
        .bucketIndex(bucket)
        .seckillPrice(activity.getSeckillPrice())
        .build();
    try {
      orderProducer.send(message);
    } catch (Exception e) {
      log.error("MQ发送失败，回补库存: orderNo={}", orderNo, e);
      stockService.compensateStock(activity.getId(), userId, bucket);
      throw new com.seckill.framework.exception.ServiceException("B000200", "系统繁忙，请稍后重试");
    }

//    log.info("秒杀下单成功: orderNo={}, userId={}, activityId={}", orderNo, userId, activity.getId());

    return SeckillOrderRespDTO.builder()
        .orderNo(orderNo)
        .status("UNPAID")
        .message("抢购成功，请支付")
        .build();
  }

  private String generateOrderNo() {
    String time = LocalDateTime.now().format(ORDER_NO_FMT);
    int random = ThreadLocalRandom.current().nextInt(100000, 999999);
    return "SK" + time + random;
  }
}
