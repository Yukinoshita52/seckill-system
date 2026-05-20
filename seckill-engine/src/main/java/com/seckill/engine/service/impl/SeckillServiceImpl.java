package com.seckill.engine.service.impl;

import static com.seckill.common.constant.RedisKeyConstants.requestKey;

import com.seckill.common.dao.entity.SeckillActivityDO;
import com.seckill.common.dao.entity.SeckillOrderDO;
import com.seckill.common.dao.mapper.SeckillOrderMapper;
import com.seckill.engine.dto.req.SeckillOrderReqDTO;
import com.seckill.engine.dto.resp.SeckillOrderRespDTO;
import com.seckill.engine.mq.OrderMessage;
import com.seckill.engine.mq.producer.SeckillOrderProducer;
import com.seckill.engine.service.SeckillService;
import com.seckill.engine.service.chain.SeckillChainContext;
import com.seckill.engine.service.chain.SeckillChainExecutor;
import com.seckill.framework.toolkit.UserContext;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

  private final SeckillOrderMapper orderMapper;
  private final SeckillChainExecutor chainExecutor;
  private final SeckillOrderProducer orderProducer;
  private final StringRedisTemplate stringRedisTemplate;

  private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  @Override
  public SeckillOrderRespDTO placeOrder(SeckillOrderReqDTO req) {
    Long userId = UserContext.getUserId();

    // 1. 责任链校验（参数 → 活动状态 → 用户去重 → 库存预检）
    SeckillChainContext chainContext = chainExecutor.execute(req);
    SeckillActivityDO activity = chainContext.getActivity();

    // 2. 生成订单号，写入 PENDING 订单
    String orderNo = generateOrderNo();
    int bucket = (int) (userId % activity.getBucketCount());
    SeckillOrderDO order = SeckillOrderDO.builder()
        .orderNo(orderNo)
        .activityId(activity.getId())
        .userId(userId)
        .seckillPrice(activity.getSeckillPrice())
        .bucketIndex(bucket)
        .status(0)
        .build();
    orderMapper.insert(order);

    // 3. 同步发送 RocketMQ → 消费者扣库存
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
      log.error("MQ 发送失败，标记订单为 FAILED: orderNo={}", orderNo, e);
      SeckillOrderDO failedOrder = new SeckillOrderDO();
      failedOrder.setOrderNo(orderNo);
      failedOrder.setStatus(3);
      orderMapper.updateById(failedOrder);
      // 删除 request key，允许用户重试
      stringRedisTemplate.delete(requestKey(activity.getId(), userId));
      throw new com.seckill.framework.exception.ServiceException("B000200", "系统繁忙，请稍后重试");
    }

    log.info("秒杀下单已受理: orderNo={}, userId={}, activityId={}", orderNo, userId, activity.getId());

    return SeckillOrderRespDTO.builder()
        .orderNo(orderNo)
        .status("PENDING")
        .message("排队中，请稍后查询结果")
        .build();
  }

  private String generateOrderNo() {
    String time = LocalDateTime.now().format(ORDER_NO_FMT);
    int random = ThreadLocalRandom.current().nextInt(100000, 999999);
    return "SK" + time + random;
  }
}
