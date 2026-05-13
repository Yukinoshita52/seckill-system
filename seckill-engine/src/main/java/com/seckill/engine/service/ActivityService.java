package com.seckill.engine.service;

import com.seckill.engine.dto.resp.ActivityQueryRespDTO;

public interface ActivityService {

  ActivityQueryRespDTO queryActivity(Long id);
}
