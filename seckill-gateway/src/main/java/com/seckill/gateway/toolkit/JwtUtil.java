package com.seckill.gateway.toolkit;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;

/** JWT 工具类 — 网关层仅需解析 token */
@Slf4j
public final class JwtUtil {

  private JwtUtil() {}

  /** 解析 JWT token，返回 Claims；解析失败返回 null */
  public static Claims parseToken(String token, String secret) {
    try {
      SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
      return Jwts.parser()
          .verifyWith(key)
          .build()
          .parseSignedClaims(token)
          .getPayload();
    } catch (Exception e) {
      log.warn("JWT 解析失败: {}", e.getMessage());
      return null;
    }
  }
}
