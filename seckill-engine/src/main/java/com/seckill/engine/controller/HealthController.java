package com.seckill.engine.controller;

import com.seckill.framework.result.Result;
import com.seckill.framework.result.Results;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

  @GetMapping("/api/health")
  public Result<Map<String, Object>> health() {
    Map<String, Object> data = new HashMap<>();
    data.put("status", "UP");
    data.put("service", "seckill-engine");
    return Results.success(data);
  }
}
