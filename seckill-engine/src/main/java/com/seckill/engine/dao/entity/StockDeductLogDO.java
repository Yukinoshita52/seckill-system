package com.seckill.engine.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
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
public class StockDeductLogDO {
  @TableId(type = IdType.AUTO)
  private Long id;

  private Long activityId;
  private Long userId;
  private Integer bucketIndex;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime deductTime;

  @TableLogic
  private Integer isDeleted;
}
