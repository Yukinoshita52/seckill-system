package com.seckill.engine.service.chain;

import com.seckill.engine.dao.entity.SeckillActivityDO;
import com.seckill.engine.dto.req.SeckillOrderReqDTO;
import lombok.Data;

/** 责任链上下文 — 在各处理器之间传递数据 */
@Data
public class SeckillChainContext {

  private final SeckillOrderReqDTO req;

  /** 链中查询到的活动信息，避免重复查库 */
  private SeckillActivityDO activity;

  public SeckillChainContext(SeckillOrderReqDTO req) {
    this.req = req;
  }
}
