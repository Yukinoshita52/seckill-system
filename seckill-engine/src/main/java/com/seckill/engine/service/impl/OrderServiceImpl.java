package com.seckill.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.common.dao.entity.SeckillActivityDO;
import com.seckill.common.dao.entity.SeckillOrderDO;
import com.seckill.common.dao.mapper.SeckillActivityMapper;
import com.seckill.common.dao.mapper.SeckillOrderMapper;
import com.seckill.engine.dto.resp.OrderStatusRespDTO;
import com.seckill.engine.service.OrderService;
import com.seckill.framework.exception.ClientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

  private final SeckillOrderMapper orderMapper;
  private final SeckillActivityMapper activityMapper;

  @Override
  public OrderStatusRespDTO queryOrderStatus(String orderNo) {
    SeckillOrderDO order = orderMapper.selectOne(
        new LambdaQueryWrapper<SeckillOrderDO>().eq(SeckillOrderDO::getOrderNo, orderNo));
    if (order == null) {
      throw new ClientException("A000100", "订单不存在");
    }

    SeckillActivityDO activity = activityMapper.selectById(order.getActivityId());
    String goodsName = activity != null ? activity.getGoodsName() : "";

    return OrderStatusRespDTO.builder()
        .orderNo(order.getOrderNo())
        .status(mapStatus(order.getStatus()))
        .seckillPrice(order.getSeckillPrice())
        .goodsName(goodsName)
        .build();
  }

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
}
