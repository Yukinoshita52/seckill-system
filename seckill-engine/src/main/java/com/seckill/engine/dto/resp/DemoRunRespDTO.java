package com.seckill.engine.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "演示触发响应")
public class DemoRunRespDTO {

  @Schema(description = "活动ID")
  private Long activityId;

  @Schema(description = "模拟请求数")
  private Integer requestCount;

  @Schema(description = "已受理请求数")
  private Integer acceptedCount;

  @Schema(description = "被拒绝请求数")
  private Integer rejectedCount;

  @Schema(description = "触发结果提示")
  private String message;
}
