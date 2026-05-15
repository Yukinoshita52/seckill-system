package com.seckill.engine.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "演示首页指标响应")
public class DemoMetricsRespDTO {

  @Schema(description = "活动ID")
  private Long activityId;

  @Schema(description = "活动名称")
  private String activityName;

  @Schema(description = "模拟请求总数，等同于该活动订单总数")
  private Long requestCount;

  @Schema(description = "排队中订单数")
  private Long pendingCount;

  @Schema(description = "待支付订单数")
  private Long unpaidCount;

  @Schema(description = "成功订单数，口径为 UNPAID + SUCCESS")
  private Long successCount;

  @Schema(description = "失败订单数")
  private Long failedCount;

  @Schema(description = "超时订单数")
  private Long timeoutCount;

  @Schema(description = "当前剩余库存")
  private Long remainStock;

  @Schema(description = "活动总库存")
  private Integer totalStock;

  @Schema(description = "按订单状态聚合的流转统计")
  private List<DemoStatusCountRespDTO> statusFlow;
}
