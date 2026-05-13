package com.seckill.engine;

import com.seckill.engine.dto.req.LoginReqDTO;
import com.seckill.engine.dto.req.RegisterReqDTO;
import com.seckill.engine.dto.resp.LoginRespDTO;
import com.seckill.engine.service.AuthService;
import com.seckill.framework.exception.ClientException;
import com.seckill.framework.toolkit.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
class AuthServiceTest {

  @Autowired private AuthService authService;

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Test
  void register_thenLogin_success() {
    // 注册
    RegisterReqDTO registerReq = new RegisterReqDTO();
    registerReq.setUsername("testuser_" + System.currentTimeMillis());
    registerReq.setPassword("123456");
    registerReq.setNickname("测试用户");
    authService.register(registerReq);
    log.info("注册成功: username={}", registerReq.getUsername());

    // 登录
    LoginReqDTO loginReq = new LoginReqDTO();
    loginReq.setUsername(registerReq.getUsername());
    loginReq.setPassword("123456");
    LoginRespDTO resp = authService.login(loginReq);

    assertNotNull(resp.getToken());
    assertNotNull(resp.getUserId());
    assertEquals(registerReq.getUsername(), resp.getUsername());
    log.info("登录成功: userId={}, username={}, token={}", resp.getUserId(), resp.getUsername(), resp.getToken());

    // 验证 token 可解析
    Claims claims = JwtUtil.parseToken(resp.getToken(), jwtSecret);
    assertNotNull(claims);
    assertEquals(String.valueOf(resp.getUserId()), claims.getSubject());
    assertEquals(resp.getUsername(), claims.get("username", String.class));
    log.info("JWT 解析成功: subject={}, username={}", claims.getSubject(), claims.get("username"));
  }

  @Test
  void register_duplicateUsername_throwsException() {
    String username = "dupuser_" + System.currentTimeMillis();

    RegisterReqDTO req = new RegisterReqDTO();
    req.setUsername(username);
    req.setPassword("123456");
    authService.register(req);

    // 重复注册
    RegisterReqDTO req2 = new RegisterReqDTO();
    req2.setUsername(username);
    req2.setPassword("654321");

    ClientException ex = assertThrows(ClientException.class, () -> authService.register(req2));
    assertEquals("用户名已存在", ex.getMessage());
    log.info("重复注册拦截成功: {}", ex.getMessage());
  }

  @Test
  void login_wrongPassword_throwsException() {
    String username = "wrongpwd_" + System.currentTimeMillis();

    RegisterReqDTO registerReq = new RegisterReqDTO();
    registerReq.setUsername(username);
    registerReq.setPassword("correct");
    authService.register(registerReq);

    LoginReqDTO loginReq = new LoginReqDTO();
    loginReq.setUsername(username);
    loginReq.setPassword("wrong");

    ClientException ex = assertThrows(ClientException.class, () -> authService.login(loginReq));
    assertEquals("用户名或密码错误", ex.getMessage());
    log.info("错误密码拦截成功: {}", ex.getMessage());
  }

  @Test
  void login_nonExistentUser_throwsException() {
    LoginReqDTO loginReq = new LoginReqDTO();
    loginReq.setUsername("nonexistent_" + System.currentTimeMillis());
    loginReq.setPassword("123456");

    ClientException ex = assertThrows(ClientException.class, () -> authService.login(loginReq));
    assertEquals("用户名或密码错误", ex.getMessage());
    log.info("不存在用户拦截成功: {}", ex.getMessage());
  }
}
