package com.seckill.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seckill.engine.dao.entity.SeckillActivityDO;
import com.seckill.engine.dao.entity.SeckillOrderDO;
import com.seckill.engine.dao.mapper.SeckillActivityMapper;
import com.seckill.engine.dao.mapper.SeckillOrderMapper;
import com.seckill.engine.dto.resp.OrderStatusRespDTO;
import com.seckill.engine.service.OrderService;
import com.seckill.engine.service.StockService;
import com.seckill.framework.exception.ClientException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

  private static final int STATUS_UNPAID = 1;
  private static final int STATUS_SUCCESS = 2;
  private static final int STATUS_TIMEOUT = 4;

  private final SeckillOrderMapper orderMapper;
  private final SeckillActivityMapper activityMapper;
  private final StockService stockService;

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

  @Override
  public void payOrder(String orderNo, Long userId) {
    SeckillOrderDO order = orderMapper.selectOne(
        new LambdaQueryWrapper<SeckillOrderDO>().eq(SeckillOrderDO::getOrderNo, orderNo));
    if (order == null) {
      throw new ClientException("A000100", "订单不存在");
    }
    if (!order.getUserId().equals(userId)) {
      throw new ClientException("A000500", "无权限操作此订单");
    }
    if (order.getStatus() != STATUS_UNPAID) {
      throw new ClientException("A000400", "订单状态不允许支付");
    }

    int affected = orderMapper.update(null,
        new LambdaUpdateWrapper<SeckillOrderDO>()
            .eq(SeckillOrderDO::getOrderNo, orderNo)
            .eq(SeckillOrderDO::getStatus, STATUS_UNPAID)
            .eq(SeckillOrderDO::getUserId, userId)
            .set(SeckillOrderDO::getStatus, STATUS_SUCCESS)
            .set(SeckillOrderDO::getPayTime, LocalDateTime.now()));

    if (affected == 0) {
      throw new ClientException("A000400", "订单状态已变更，支付失败");
    }
    log.info("订单支付成功: orderNo={}, userId={}", orderNo, userId);
  }

  @Override
  public void cancelOrder(String orderNo, Long userId) {
    SeckillOrderDO order = orderMapper.selectOne(
        new LambdaQueryWrapper<SeckillOrderDO>().eq(SeckillOrderDO::getOrderNo, orderNo));
    if (order == null) {
      throw new ClientException("A000100", "订单不存在");
    }
    if (!order.getUserId().equals(userId)) {
      throw new ClientException("A000500", "无权限操作此订单");
    }
    if (order.getStatus() != STATUS_UNPAID) {
      throw new ClientException("A000400", "订单状态不允许取消");
    }

    // 取消订单 → 状态设为 TIMEOUT，回补库存
    int affected = orderMapper.update(null,
        new LambdaUpdateWrapper<SeckillOrderDO>()
            .eq(SeckillOrderDO::getOrderNo, orderNo)
            .eq(SeckillOrderDO::getStatus, STATUS_UNPAID)
            .eq(SeckillOrderDO::getUserId, userId)
            .set(SeckillOrderDO::getStatus, STATUS_TIMEOUT));

    if (affected == 0) {
      throw new ClientException("A000400", "订单状态已变更，取消失败");
    }

    stockService.compensateStock(order.getActivityId(), userId, order.getBucketIndex());
    log.info("订单已取消: orderNo={}, userId={}", orderNo, userId);
  }

  @Override
  public List<OrderStatusRespDTO> listByUser(Long userId) {
    List<SeckillOrderDO> orders = orderMapper.selectList(
        new LambdaQueryWrapper<SeckillOrderDO>()
            .eq(SeckillOrderDO::getUserId, userId)
            .in(SeckillOrderDO::getStatus, STATUS_UNPAID, STATUS_SUCCESS, STATUS_TIMEOUT)
            .orderByDesc(SeckillOrderDO::getCreateTime));

    return orders.stream().map(order -> {
      SeckillActivityDO activity = activityMapper.selectById(order.getActivityId());
      String goodsName = activity != null ? activity.getGoodsName() : "";
      return OrderStatusRespDTO.builder()
          .orderNo(order.getOrderNo())
          .status(mapStatus(order.getStatus()))
          .seckillPrice(order.getSeckillPrice())
          .goodsName(goodsName)
          .build();
    }).toList();
  }

  private String mapStatus(Integer status) {
    return switch (status) {
      case 1 -> "UNPAID";
      case 2 -> "SUCCESS";
      case 4 -> "TIMEOUT";
      default -> "UNKNOWN";
    };
  }
}