package com.seckill.admin;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.seckill.admin.dto.req.ActivityCreateReqDTO;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class ActivityInitTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private StringRedisTemplate redisTemplate;

  @Test
  void createAndInitCache() throws Exception {
    // 构造请求：活动立即开始，1小时后过期
    ActivityCreateReqDTO req = ActivityCreateReqDTO.builder()
        .activityName("测试秒杀活动-" + System.currentTimeMillis())
        .goodsId(9999L)
        .goodsName("测试商品")
        .originalPrice(new BigDecimal("999.00"))
        .seckillPrice(new BigDecimal("1.00"))
        .totalStock(5)
        .bucketCount(5)
        .startTime(LocalDateTime.now())
        .endTime(LocalDateTime.now().plusHours(1))
        .build();

    // 1. 调用 POST /api/admin/activity 创建活动
    String response = mockMvc.perform(post("/api/admin/activity")
            .contentType(MediaType.APPLICATION_JSON)
            .header("X-User-Id", "99999")
            .header("X-Username", "admin")
            .content(toJson(req)))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

    // 提取返回的 activityId（简单解析 JSON 中的 id 字段）
    Long activityId = extractIdFromJson(response);
    assertNotNull(activityId, "活动ID不应为空");

    // 2. 调用 POST /api/admin/activity/{id}/init-cache 初始化缓存
    mockMvc.perform(post("/api/admin/activity/{id}/init-cache", activityId)
            .header("X-User-Id", "1")
            .header("X-Username", "admin"))
        .andExpect(status().isOk());

    // 3. 验证 Redis 中缓存已写入
    String activityKey = "activity:" + activityId;
    String totalKey = "total:" + activityId;

    assertTrue(Boolean.TRUE.equals(redisTemplate.hasKey(activityKey)),
        "活动缓存未写入: " + activityKey);
    assertTrue(Boolean.TRUE.equals(redisTemplate.hasKey(totalKey)),
        "总库存缓存未写入: " + totalKey);
    assertEquals("5", redisTemplate.opsForValue().get(totalKey),
        "总库存值不正确");

    // 验证分桶库存
    for (int i = 0; i < 5; i++) {
      String bucketKey = "stock:" + activityId + ":" + i;
      assertTrue(Boolean.TRUE.equals(redisTemplate.hasKey(bucketKey)),
          "分桶缓存未写入: " + bucketKey);
    }

    System.out.println("测试通过: activityId=" + activityId);
  }

  private String toJson(Object obj) {
    try {
      com.fasterxml.jackson.databind.ObjectMapper mapper =
          new com.fasterxml.jackson.databind.ObjectMapper();
      mapper.registerModule(
          new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
      return mapper.writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Long extractIdFromJson(String json) {
    // 简单解析 {"code":"000000","data":{"id":123,...}}}
    String marker = "\"id\":";
    int idx = json.indexOf(marker);
    if (idx < 0) return null;
    int start = idx + marker.length();
    while (start < json.length() && !Character.isDigit(json.charAt(start))) start++;
    int end = start;
    while (end < json.length() && Character.isDigit(json.charAt(end))) end++;
    return Long.parseLong(json.substring(start, end));
  }
}
