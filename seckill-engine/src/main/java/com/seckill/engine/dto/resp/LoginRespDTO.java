package com.seckill.engine.dto.resp;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginRespDTO {
  @Schema(description = "JWT token")
  private String token;

  @Schema(description = "用户ID")
  private Long userId;

  @Schema(description = "用户名")
  private String username;
}
