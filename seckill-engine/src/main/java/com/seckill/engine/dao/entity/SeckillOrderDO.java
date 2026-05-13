package com.seckill.engine.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
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
@TableName("t_seckill_order")
@Schema(description = "秒杀订单")
public class SeckillOrderDO {
  @TableId(type = IdType.AUTO)
  @Schema(description = "订单ID")
  private Long id;

  @Schema(description = "订单号")
  private String orderNo;

  @Schema(description = "活动ID")
  private Long activityId;

  @Schema(description = "用户ID")
  private Long userId;

  @Schema(description = "秒杀价")
  private BigDecimal seckillPrice;

  @Schema(description = "库存桶索引")
  private Integer bucketIndex;

  @Schema(description = "状态: 0-待支付 1-已支付 2-已取消 3-已超时")
  private Integer status;

  @TableField(fill = FieldFill.INSERT)
  @Schema(description = "创建时间")
  private LocalDateTime createTime;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  @Schema(description = "更新时间")
  private LocalDateTime updateTime;

  @Schema(description = "支付时间")
  private LocalDateTime payTime;

  @TableLogic
  @Schema(description = "逻辑删除: 0-未删除 1-已删除")
  private Integer isDeleted;
}
