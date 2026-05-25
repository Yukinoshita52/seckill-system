package com.seckill.engine.service;

import com.seckill.engine.dto.resp.ActivityQueryRespDTO;
import com.seckill.engine.dto.resp.SoldOutCheckRespDTO;
import java.util.List;

/** 活动服务 */
public interface ActivityService {

  /**
   * 查询活动列表（含 Redis 实时库存）
   *
   * @return 活动列表
   */
  List<ActivityQueryRespDTO> listActivities();

  /**
   * 检查活动是否已售罄（通过DB已支付订单数与库存量对比）
   *
   * @param activityId 活动ID
   * @return 售罄检查结果
   */
  SoldOutCheckRespDTO checkSoldOut(Long activityId);
}
