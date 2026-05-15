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
@Schema(description = "演示接口状态计数")
public class DemoStatusCountRespDTO {

  @Schema(description = "订单状态")
  private String status;

  @Schema(description = "该状态下的订单数量")
  private Long count;
}
