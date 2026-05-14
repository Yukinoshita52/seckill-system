package com.seckill.common.dao.entity;

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
@TableName("t_stock_deduct_log")
@Schema(description = "库存扣减日志")
public class StockDeductLogDO {
  @TableId(type = IdType.AUTO)
  @Schema(description = "日志ID")
  private Long id;

  @Schema(description = "活动ID")
  private Long activityId;

  @Schema(description = "用户ID")
  private Long userId;

  @Schema(description = "库存桶索引")
  private Integer bucketIndex;

  @TableField(fill = FieldFill.INSERT)
  @Schema(description = "扣减时间")
  private LocalDateTime deductTime;

  @TableLogic
  @Schema(description = "逻辑删除: 0-未删除 1-已删除")
  private Integer isDeleted;
}
