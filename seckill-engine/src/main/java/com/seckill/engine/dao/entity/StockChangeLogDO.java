package com.seckill.engine.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_stock_change_log")
@Schema(description = "库存变化日志")
public class StockChangeLogDO {
  @TableId(type = IdType.AUTO)
  @Schema(description = "日志ID")
  private Long id;

  @Schema(description = "活动ID")
  private Long activityId;

  @Schema(description = "用户ID")
  private Long userId;

  @Schema(description = "关联订单号")
  private String orderNo;

  @Schema(description = "变化类型: 0-扣减 1-回补")
  private Integer changeType;

  @Schema(description = "变化数量")
  private Integer changeQuantity;

  @Schema(description = "分桶索引")
  private Integer bucketIndex;

  @TableField(fill = FieldFill.INSERT)
  @Schema(description = "变化时间")
  private LocalDateTime changeTime;

  @TableLogic
  @Schema(description = "逻辑删除: 0-未删除 1-已删除")
  private Integer isDeleted;
}