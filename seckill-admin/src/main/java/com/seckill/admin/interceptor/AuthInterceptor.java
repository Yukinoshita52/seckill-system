package com.seckill.admin.interceptor;

import com.seckill.framework.exception.ClientException;
import com.seckill.framework.toolkit.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 用户上下文拦截器 — 从网关传递的 X-User-Id / X-Username header 中填充 UserContext。
 * 认证由网关层统一处理，此处仅提取用户信息。
 */
@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
    String userId = request.getHeader("X-User-Id");
    String username = request.getHeader("X-Username");

    if (userId == null || userId.isEmpty()) {
      throw new ClientException("A000500", "未登录");
    }

    UserContext.setUserId(Long.parseLong(userId));
    UserContext.setUsername(username);

    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
    UserContext.clear();
  }
}
