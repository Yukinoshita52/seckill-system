package com.seckill.engine.service;

import com.seckill.engine.dto.resp.DemoMetricsRespDTO;

public interface DemoMetricsService {

  DemoMetricsRespDTO queryDemoMetrics(Long activityId);
}
