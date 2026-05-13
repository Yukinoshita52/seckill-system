package com.seckill.engine.interceptor;

import com.seckill.framework.exception.ClientException;
import com.seckill.framework.toolkit.JwtUtil;
import com.seckill.framework.toolkit.UserContext;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** 认证拦截器 — 从 JWT token 解析用户信息并填充 UserContext */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new ClientException("A000500", "未登录");
    }

    String token = authHeader.substring(7);
    Claims claims = JwtUtil.parseToken(token, jwtSecret);
    if (claims == null) {
      throw new ClientException("A000500", "登录已过期");
    }

    Long userId = Long.parseLong(claims.getSubject());
    String username = claims.get("username", String.class);
    UserContext.setUserId(userId);
    UserContext.setUsername(username);

    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    UserContext.clear();
  }
}
