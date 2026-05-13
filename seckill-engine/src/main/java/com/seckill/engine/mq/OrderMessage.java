package com.seckill.engine.mq;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** 秒杀订单消息体 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderMessage implements Serializable {

  private static final long serialVersionUID = 1L;

  private String orderNo;
  private Long activityId;
  private Long userId;
  private Integer bucketIndex;
}
