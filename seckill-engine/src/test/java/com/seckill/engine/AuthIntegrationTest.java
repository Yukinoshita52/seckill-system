package com.seckill.engine;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seckill.engine.dto.req.RegisterReqDTO;
import com.seckill.engine.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestConfig.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AuthIntegrationTest {

  @Autowired private MockMvc mockMvc;
  @Autowired private AuthService authService;
  @Autowired private ObjectMapper objectMapper;

  private static final String TEST_USERNAME = "mockmvc_user_" + System.currentTimeMillis();
  private static final String TEST_PASSWORD = "test123456";

  @BeforeAll
  void setupUser() {
    RegisterReqDTO req = new RegisterReqDTO();
    req.setUsername(TEST_USERNAME);
    req.setPassword(TEST_PASSWORD);
    req.setNickname("MockMvc测试用户");
    authService.register(req);
    log.info("测试用户已注册: {}", TEST_USERNAME);
  }

  @Test
  void login_success() throws Exception {
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"" + TEST_USERNAME + "\",\"password\":\"" + TEST_PASSWORD + "\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("0"))
        .andExpect(jsonPath("$.data.token").isNotEmpty())
        .andExpect(jsonPath("$.data.userId").isNotEmpty())
        .andExpect(jsonPath("$.data.username").value(TEST_USERNAME));
    log.info("登录接口测试通过");
  }

  @Test
  void login_wrongPassword_returnsError() throws Exception {
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"" + TEST_USERNAME + "\",\"password\":\"wrong\"}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("A000500"))
        .andExpect(jsonPath("$.message").value("用户名或密码错误"));
    log.info("登录错误密码测试通过");
  }

  @Test
  void accessProtectedEndpoint_withoutToken_returns401() throws Exception {
    mockMvc.perform(post("/api/seckill/order")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"activityId\":1}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("A000500"))
        .andExpect(jsonPath("$.message").value("未登录"));
    log.info("无 token 拦截测试通过");
  }

  @Test
  void accessProtectedEndpoint_withInvalidToken_returnsError() throws Exception {
    mockMvc.perform(post("/api/seckill/order")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer invalid.token.here")
            .content("{\"activityId\":1}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("A000500"))
        .andExpect(jsonPath("$.message").value("登录已过期"));
    log.info("无效 token 拦截测试通过");
  }

  @Test
  void accessProtectedEndpoint_withValidToken_passesAuth() throws Exception {
    // 先登录拿 token
    MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"" + TEST_USERNAME + "\",\"password\":\"" + TEST_PASSWORD + "\"}"))
        .andExpect(status().isOk())
        .andReturn();

    JsonNode json = objectMapper.readTree(loginResult.getResponse().getContentAsString());
    String token = json.get("data").get("token").asText();
    log.info("获取到 token: {}", token);

    // 带 token 请求秒杀接口（会因活动不存在等业务原因失败，但不会返回"未登录"）
    mockMvc.perform(post("/api/seckill/order")
            .contentType(MediaType.APPLICATION_JSON)
            .header("Authorization", "Bearer " + token)
            .content("{\"activityId\":99999}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("A000100"))
        .andExpect(jsonPath("$.message").value("活动不存在"));
    log.info("带 token 请求测试通过（通过认证，业务层拦截）");
  }

  @Test
  void accessLoginEndpoint_withoutToken_isAllowed() throws Exception {
    mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"test\",\"password\":\"test\"}"))
        .andExpect(status().isOk());
    log.info("登录接口白名单放行测试通过");
  }
}
