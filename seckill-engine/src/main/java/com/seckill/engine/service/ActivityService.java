package com.seckill.engine.service;

import com.seckill.engine.dto.resp.ActivityQueryRespDTO;

/** 活动服务 */
public interface ActivityService {

  /** 查询活动详情（含 Redis 实时库存） */
  ActivityQueryRespDTO queryActivity(Long id);
}
