package com.seckill.engine;

import com.seckill.framework.toolkit.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class JwtUtilTest {

  private static final String SECRET = "test-secret-key-must-be-at-least-32-bytes-long-for-hs256";

  @Test
  void generateAndParseToken_success() {
    String token = JwtUtil.generateToken(1001L, "testuser", SECRET, 3600);
    assertNotNull(token);
    log.info("生成的 token: {}", token);

    Claims claims = JwtUtil.parseToken(token, SECRET);
    assertNotNull(claims);
    assertEquals("1001", claims.getSubject());
    assertEquals("testuser", claims.get("username"));
    assertNotNull(claims.getExpiration());
    log.info("解析成功: userId={}, username={}, expire={}", claims.getSubject(), claims.get("username"), claims.getExpiration());
  }

  @Test
  void parseToken_invalidToken_returnsNull() {
    Claims claims = JwtUtil.parseToken("invalid.token.here", SECRET);
    assertNull(claims);
    log.info("无效 token 解析返回 null");
  }

  @Test
  void parseToken_wrongSecret_returnsNull() {
    String token = JwtUtil.generateToken(1L, "user", SECRET, 3600);
    Claims claims = JwtUtil.parseToken(token, "wrong-secret-key-must-be-at-least-32-bytes");
    assertNull(claims);
    log.info("错误密钥解析返回 null");
  }
}
