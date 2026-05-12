package com.seckill.engine.dao.entity;

import com.baomidou.mybatisplus.annotation.*;
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
public class SeckillOrderDO {
  @TableId(type = IdType.AUTO)
  private Long id;

  private String orderNo;
  private Long activityId;
  private Long userId;
  private BigDecimal seckillPrice;
  private Integer bucketIndex;
  private Integer status;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;

  private LocalDateTime payTime;

  @TableLogic
  private Integer isDeleted;
}
