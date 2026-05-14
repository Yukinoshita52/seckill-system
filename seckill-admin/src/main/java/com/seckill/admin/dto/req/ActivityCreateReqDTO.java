package com.seckill.admin.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "创建活动请求")
public class ActivityCreateReqDTO {

  @NotBlank(message = "活动名称不能为空")
  @Schema(description = "活动名称")
  private String activityName;

  @NotNull(message = "商品ID不能为空")
  @Schema(description = "商品ID")
  private Long goodsId;

  @NotBlank(message = "商品名称不能为空")
  @Schema(description = "商品名称")
  private String goodsName;

  @NotNull(message = "原价不能为空")
  @Schema(description = "原价")
  private BigDecimal originalPrice;

  @NotNull(message = "秒杀价不能为空")
  @Schema(description = "秒杀价")
  private BigDecimal seckillPrice;

  @NotNull(message = "总库存不能为空")
  @Schema(description = "总库存")
  private Integer totalStock;

  @NotNull(message = "库存桶数量不能为空")
  @Schema(description = "库存桶数量")
  private Integer bucketCount;

  @NotNull(message = "开始时间不能为空")
  @Schema(description = "开始时间")
  private LocalDateTime startTime;

  @NotNull(message = "结束时间不能为空")
  @Schema(description = "结束时间")
  private LocalDateTime endTime;
}
