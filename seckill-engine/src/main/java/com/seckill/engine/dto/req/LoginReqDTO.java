package com.seckill.engine.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录请求")
public class LoginReqDTO {
  @Schema(description = "用户名", requiredMode = Schema.RequiredMode.REQUIRED)
  private String username;

  @Schema(description = "密码", requiredMode = Schema.RequiredMode.REQUIRED)
  private String password;
}
