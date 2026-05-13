package com.seckill.engine.service;

import com.seckill.engine.dto.req.SeckillOrderReqDTO;
import com.seckill.engine.dto.resp.SeckillOrderRespDTO;

/** 秒杀核心服务 */
public interface SeckillService {

  /**
   * 秒杀下单：责任链校验 → 扣减库存 → 创建订单
   *
   * @param req 下单请求
   * @return 下单结果
   */
  SeckillOrderRespDTO placeOrder(SeckillOrderReqDTO req);
}
