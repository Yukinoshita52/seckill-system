package com.seckill.engine.service;

import com.seckill.engine.dto.resp.OrderStatusRespDTO;
import java.util.List;

/** 订单服务 */
public interface OrderService {

  /**
   * 查询订单状态
   *
   * @param orderNo 订单号
   * @return 订单状态
   */
  OrderStatusRespDTO queryOrderStatus(String orderNo);

  /**
   * 支付订单（UNPAID → SUCCESS）
   *
   * @param orderNo 订单号
   * @param userId 用户ID
   */
  void payOrder(String orderNo, Long userId);

  /**
   * 取消订单（UNPAID → FAILED，回补库存）
   *
   * @param orderNo 订单号
   * @param userId 用户ID
   */
  void cancelOrder(String orderNo, Long userId);

  /**
   * 查询用户的所有订单
   *
   * @param userId 用户ID
   * @return 订单列表
   */
  List<OrderStatusRespDTO> listByUser(Long userId);
}
