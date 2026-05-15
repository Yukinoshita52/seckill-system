package com.seckill.engine.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "演示触发请求")
public class DemoRunReqDTO {

  @Schema(description = "活动ID", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long activityId;

  @Schema(description = "模拟请求数", requiredMode = Schema.RequiredMode.REQUIRED)
  private Integer requestCount;
}
