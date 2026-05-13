package com.seckill.engine.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "订单状态响应")
public class OrderStatusRespDTO {
  @Schema(description = "订单号")
  private String orderNo;

  @Schema(description = "状态: PENDING/SUCCESS/FAILED/TIMEOUT")
  private String status;

  @Schema(description = "秒杀价")
  private BigDecimal seckillPrice;

  @Schema(description = "商品名称")
  private String goodsName;
}
