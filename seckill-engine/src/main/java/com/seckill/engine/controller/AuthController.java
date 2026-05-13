package com.seckill.engine.controller;

import com.seckill.engine.dto.req.LoginReqDTO;
import com.seckill.engine.dto.req.RegisterReqDTO;
import com.seckill.engine.dto.resp.LoginRespDTO;
import com.seckill.engine.service.AuthService;
import com.seckill.framework.result.Result;
import com.seckill.framework.result.Results;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "认证")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @Operation(summary = "用户登录")
  @PostMapping("/login")
  public Result<LoginRespDTO> login(@RequestBody LoginReqDTO req) {
    return Results.success(authService.login(req));
  }

  @Operation(summary = "用户注册")
  @PostMapping("/register")
  public Result<Void> register(@RequestBody RegisterReqDTO req) {
    authService.register(req);
    return Results.success(null);
  }
}
