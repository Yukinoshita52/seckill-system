package com.seckill.engine.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "秒杀下单请求")
public class SeckillOrderReqDTO {
  @Schema(description = "活动ID", requiredMode = Schema.RequiredMode.REQUIRED)
  private Long activityId;

  @Schema(description = "验证码Token")
  private String captchaToken;

  @Schema(description = "验证码")
  private String captchaCode;
}
