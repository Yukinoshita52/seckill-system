package com.seckill.engine.service;

import com.seckill.engine.dto.resp.ActivityQueryRespDTO;
import java.util.List;

/** 活动服务 */
public interface ActivityService {

  /**
   * 查询活动列表（含 Redis 实时库存）
   *
   * @return 活动列表
   */
  List<ActivityQueryRespDTO> listActivities();
}
