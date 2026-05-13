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
@Schema(description = "秒杀下单响应")
public class SeckillOrderRespDTO {
  @Schema(description = "订单号")
  private String orderNo;

  @Schema(description = "排队状态: PENDING/SUCCESS/FAILED")
  private String status;

  @Schema(description = "提示信息")
  private String message;
}
