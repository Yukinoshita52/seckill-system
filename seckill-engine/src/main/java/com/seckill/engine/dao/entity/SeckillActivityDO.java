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
@TableName("t_seckill_activity")
public class SeckillActivityDO {
  @TableId(type = IdType.AUTO)
  private Long id;

  private String activityName;
  private Long goodsId;
  private String goodsName;
  private BigDecimal originalPrice;
  private BigDecimal seckillPrice;
  private Integer totalStock;
  private Integer bucketCount;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Integer status;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createTime;

  @TableField(fill = FieldFill.INSERT_UPDATE)
  private LocalDateTime updateTime;

  @TableLogic
  private Integer isDeleted;
}
