package com.seckill.engine.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "活动售罄检查响应")
public class SoldOutCheckRespDTO {

  @Schema(description = "活动ID")
  private Long activityId;

  @Schema(description = "是否已售罄")
  private Boolean soldOut;
}
