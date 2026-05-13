package com.seckill.engine.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "活动查询响应")
public class ActivityQueryRespDTO {
  @Schema(description = "活动ID")
  private Long id;

  @Schema(description = "活动名称")
  private String activityName;

  @Schema(description = "商品名称")
  private String goodsName;

  @Schema(description = "原价")
  private BigDecimal originalPrice;

  @Schema(description = "秒杀价")
  private BigDecimal seckillPrice;

  @Schema(description = "总库存")
  private Integer totalStock;

  @Schema(description = "剩余库存")
  private Long remainStock;

  @Schema(description = "开始时间")
  private LocalDateTime startTime;

  @Schema(description = "结束时间")
  private LocalDateTime endTime;

  @Schema(description = "状态: 0-未开始 1-进行中 2-已结束")
  private Integer status;
}
