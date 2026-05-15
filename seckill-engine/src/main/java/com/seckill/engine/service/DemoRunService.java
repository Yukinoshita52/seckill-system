package com.seckill.engine.service;

import com.seckill.engine.dto.req.DemoRunReqDTO;
import com.seckill.engine.dto.resp.DemoRunRespDTO;

public interface DemoRunService {

  DemoRunRespDTO runDemo(DemoRunReqDTO req);
}
