package com.seckill.admin.controller;

import com.seckill.admin.dto.req.ActivityCreateReqDTO;
import com.seckill.admin.dto.req.ActivityUpdateReqDTO;
import com.seckill.admin.dto.resp.ActivityRespDTO;
import com.seckill.admin.service.ActivityAdminService;
import com.seckill.framework.result.Result;
import com.seckill.framework.result.Results;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "活动管理")
@RestController
@RequestMapping("/api/admin/activity")
@RequiredArgsConstructor
public class ActivityController {

  private final ActivityAdminService activityAdminService;

  @Operation(summary = "创建活动")
  @PostMapping
  public Result<ActivityRespDTO> create(@Valid @RequestBody ActivityCreateReqDTO req) {
    return Results.success(activityAdminService.create(req));
  }

  @Operation(summary = "更新活动")
  @PutMapping("/{id}")
  public Result<ActivityRespDTO> update(@PathVariable Long id, @RequestBody ActivityUpdateReqDTO req) {
    return Results.success(activityAdminService.update(id, req));
  }

  @Operation(summary = "查询活动详情")
  @GetMapping("/{id}")
  public Result<ActivityRespDTO> getById(@PathVariable Long id) {
    return Results.success(activityAdminService.getById(id));
  }

  @Operation(summary = "活动列表")
  @GetMapping("/list")
  public Result<List<ActivityRespDTO>> list() {
    return Results.success(activityAdminService.list());
  }

  @Operation(summary = "初始化活动缓存（Redis预热）")
  @PostMapping("/{id}/init-cache")
  public Result<Void> initCache(@PathVariable Long id) {
    activityAdminService.initCache(id);
    return Results.success();
  }
}
