package com.seckill.framework.toolkit;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;

/** JWT 工具类 — 生成和解析 token */
@Slf4j
public final class JwtUtil {

  private JwtUtil() {}

  /** 生成 JWT token */
  public static String generateToken(Long userId, String username, String secret, long expirationSeconds) {
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    Date now = new Date();
    Date expireAt = new Date(now.getTime() + expirationSeconds * 1000);
    return Jwts.builder()
        .subject(String.valueOf(userId))
        .claim("username", username)
        .issuedAt(now)
        .expiration(expireAt)
        .signWith(key)
        .compact();
  }

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
