package com.seckill.engine.controller;

import com.seckill.engine.dto.resp.ActivityQueryRespDTO;
import com.seckill.engine.service.ActivityService;
import com.seckill.framework.result.Result;
import com.seckill.framework.result.Results;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "活动管理")
@RestController
@RequestMapping("/api/activity")
@RequiredArgsConstructor
public class ActivityController {

  private final ActivityService activityService;

  @Operation(summary = "查询活动列表")
  @GetMapping("/list")
  public Result<List<ActivityQueryRespDTO>> listActivities() {
    return Results.success(activityService.listActivities());
  }

  @Operation(summary = "查询活动详情")
  @GetMapping("/{id}")
  public Result<ActivityQueryRespDTO> queryActivity(@PathVariable Long id) {
    return Results.success(activityService.queryActivity(id));
  }
}
