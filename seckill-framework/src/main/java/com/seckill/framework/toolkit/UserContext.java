package com.seckill.framework.toolkit;

// todo: 需实现认证过滤器（AuthFilter），在请求进入时从 token/session 解析用户并填充 UserContext
// todo: 配合 AuthFilter，在请求结束时（afterCompletion）调用 clear() 防止 ThreadLocal 泄漏
public final class UserContext {
  private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
  private static final ThreadLocal<String> USERNAME = new ThreadLocal<>();

  public static void setUserId(Long userId) {
    USER_ID.set(userId);
  }

  public static Long getUserId() {
    return USER_ID.get();
  }

  public static void setUsername(String username) {
    USERNAME.set(username);
  }

  public static String getUsername() {
    return USERNAME.get();
  }

  public static void clear() {
    USER_ID.remove();
    USERNAME.remove();
  }
}
