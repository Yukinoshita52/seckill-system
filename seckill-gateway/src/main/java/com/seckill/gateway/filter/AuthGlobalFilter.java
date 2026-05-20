package com.seckill.gateway.filter;

import com.seckill.gateway.config.JwtProperties;
import com.seckill.gateway.toolkit.JwtUtil;
import io.jsonwebtoken.Claims;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthGlobalFilter implements GlobalFilter, Ordered {

  private final JwtProperties jwtProperties;

  private static final List<String> WHITE_LIST = List.of(
      "/api/auth/login",
      "/api/auth/register",
      "/api/activity/list",
      "/api/activity/",
      "/api/demo/",
      "/doc.html",
      "/swagger-ui",
      "/v3/api-docs",
      "/webjars");

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    ServerHttpRequest request = exchange.getRequest();

    // OPTIONS 预检请求直接放行
    if (request.getMethod() == HttpMethod.OPTIONS) {
      return chain.filter(exchange);
    }

    String path = request.getURI().getPath();

    // 白名单放行
    for (String white : WHITE_LIST) {
      if (path.startsWith(white)) {
        return chain.filter(exchange);
      }
    }

    // 校验 Authorization header
    String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      return unauthorized(exchange, "未登录");
    }

    String token = authHeader.substring(7);
    Claims claims = JwtUtil.parseToken(token, jwtProperties.getSecret());
    if (claims == null) {
      return unauthorized(exchange, "登录已过期");
    }

    // 将用户信息传递给下游服务
    ServerHttpRequest mutatedRequest = request.mutate()
        .header("X-User-Id", claims.getSubject())
        .header("X-Username", claims.get("username", String.class))
        .build();

    return chain.filter(exchange.mutate().request(mutatedRequest).build());
  }

  @Override
  public int getOrder() {
    return -100;
  }

  private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

    String body = "{\"code\":\"A000500\",\"message\":\"" + message + "\",\"data\":null,\"requestId\":\"\"}";
    DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
    return response.writeWith(Mono.just(buffer));
  }
}