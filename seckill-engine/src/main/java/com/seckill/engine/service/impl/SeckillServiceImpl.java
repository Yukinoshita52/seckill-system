package com.seckill.engine.service.impl;

import com.seckill.engine.dao.entity.SeckillActivityDO;
import com.seckill.engine.dao.entity.SeckillOrderDO;
import com.seckill.engine.dao.mapper.SeckillActivityMapper;
import com.seckill.engine.dao.mapper.SeckillOrderMapper;
import com.seckill.engine.dto.req.SeckillOrderReqDTO;
import com.seckill.engine.dto.resp.SeckillOrderRespDTO;
import com.seckill.engine.service.SeckillService;
import com.seckill.engine.service.StockService;
import com.seckill.framework.exception.ClientException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

  private final SeckillActivityMapper activityMapper;
  private final SeckillOrderMapper orderMapper;
  private final StockService stockService;

  private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

  @Override
  @Transactional
  public SeckillOrderRespDTO placeOrder(SeckillOrderReqDTO req) {
    // todo: 接入责任链校验：验证码 -> 活动状态 -> 用户去重 -> 库存预检
    // todo: 活动信息应从 Redis 缓存读取，避免每次秒杀请求查库；缓存失效时回源 DB
    // todo: 将下单请求投递到本地队列 + RocketMQ，改为异步下单；当前为同步直接返回结果
    // todo: userId 应从登录态/UserContext 获取，不应由客户端传入

    // 1. 校验活动
    SeckillActivityDO activity = activityMapper.selectById(req.getActivityId());
    if (activity == null) {
      throw new ClientException("A000100", "活动不存在");
    }

    LocalDateTime now = LocalDateTime.now();
    if (now.isBefore(activity.getStartTime())) {
      throw new ClientException("A000200", "活动未开始");
    }
    if (now.isAfter(activity.getEndTime())) {
      throw new ClientException("A000300", "活动已结束");
    }

    // 2. 扣减库存（Lua 原子操作，含去重校验）
    long remaining = stockService.deductStock(activity.getId(), req.getUserId());

    // 3. 生成订单号
    String orderNo = generateOrderNo();

    // 4. 创建订单
    // todo: 桶数量应在两处保持一致；当前 StockServiceImpl 硬编码为 5，此处从活动读取
    int bucket = (int) (req.getUserId() % activity.getBucketCount());
    SeckillOrderDO order = SeckillOrderDO.builder()
        .orderNo(orderNo)
        .activityId(activity.getId())
        .userId(req.getUserId())
        .seckillPrice(activity.getSeckillPrice())
        .bucketIndex(bucket)
        .status(0) // todo: 订单状态应定义为枚举常量（如 OrderStatus.PENDING），避免魔法数字
        .build();
    orderMapper.insert(order);

    log.info("秒杀下单成功: orderNo={}, userId={}, activityId={}, remaining={}",
        orderNo, req.getUserId(), activity.getId(), remaining);

    return SeckillOrderRespDTO.builder()
        .orderNo(orderNo)
        .status("SUCCESS")
        .message("下单成功")
        .build();
  }

  private String generateOrderNo() {
    String time = LocalDateTime.now().format(ORDER_NO_FMT);
    int random = ThreadLocalRandom.current().nextInt(100000, 999999);
    return "SK" + time + random;
  }
}
