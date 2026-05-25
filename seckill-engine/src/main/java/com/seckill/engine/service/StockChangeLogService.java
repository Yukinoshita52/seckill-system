package com.seckill.engine.service;

import com.seckill.engine.dao.entity.StockChangeLogDO;

/** 库存变化日志服务 */
public interface StockChangeLogService {

  /** 记录库存变化 */
  void log(StockChangeLogDO log);
}