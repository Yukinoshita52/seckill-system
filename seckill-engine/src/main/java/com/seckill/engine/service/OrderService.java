package com.seckill.engine.service;

import com.seckill.engine.dto.resp.OrderStatusRespDTO;

/** 订单服务 */
public interface OrderService {

  /**
   * 查询订单状态
   *
   * @param orderNo 订单号
   * @return 订单状态
   */
  OrderStatusRespDTO queryOrderStatus(String orderNo);
}
