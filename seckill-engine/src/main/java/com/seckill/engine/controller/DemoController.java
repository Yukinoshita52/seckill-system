package com.seckill.engine.controller;

import com.seckill.engine.dto.req.DemoRunReqDTO;
import com.seckill.engine.dto.resp.DemoMetricsRespDTO;
import com.seckill.engine.dto.resp.DemoRunRespDTO;
import com.seckill.engine.service.DemoMetricsService;
import com.seckill.engine.service.DemoRunService;
import com.seckill.framework.result.Result;
import com.seckill.framework.result.Results;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "演示")
@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

  private final DemoMetricsService demoMetricsService;
  private final DemoRunService demoRunService;

  @Operation(summary = "查询演示首页实时指标")
  @GetMapping("/metrics/{activityId}")
  public Result<DemoMetricsRespDTO> queryDemoMetrics(@PathVariable Long activityId) {
    return Results.success(demoMetricsService.queryDemoMetrics(activityId));
  }

  @Operation(summary = "触发一轮演示请求")
  @PostMapping("/run")
  public Result<DemoRunRespDTO> runDemo(@RequestBody DemoRunReqDTO req) {
    return Results.success(demoRunService.runDemo(req));
  }
}
