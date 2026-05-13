package com.seckill.engine.service;

import com.seckill.engine.dto.req.SeckillOrderReqDTO;
import com.seckill.engine.dto.resp.SeckillOrderRespDTO;

/** 秒杀核心服务 */
public interface SeckillService {

  /** 秒杀下单：校验活动 → 扣减库存 → 创建订单 */
  SeckillOrderRespDTO placeOrder(SeckillOrderReqDTO req);
}
