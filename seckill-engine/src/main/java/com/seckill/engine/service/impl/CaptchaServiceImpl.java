package com.seckill.engine.service.impl;

import com.seckill.framework.exception.ClientException;
import com.seckill.engine.common.constant.RedisKeyConstants;
import com.seckill.engine.dto.resp.CaptchaRespDTO;
import com.seckill.engine.service.CaptchaService;
import com.wf.captcha.SpecCaptcha;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CaptchaServiceImpl implements CaptchaService {

  private final StringRedisTemplate stringRedisTemplate;

  private static final long CAPTCHA_TTL_MINUTES = 5;

  @Override
  public CaptchaRespDTO generateCaptcha() {
    SpecCaptcha captcha = new SpecCaptcha(130, 48, 4);
    String token = UUID.randomUUID().toString();
    String code = captcha.text().toLowerCase();
    stringRedisTemplate.opsForValue()
        .set(RedisKeyConstants.captchaKey(token), code, CAPTCHA_TTL_MINUTES, TimeUnit.MINUTES);
    return CaptchaRespDTO.builder()
        .captchaToken(token)
        .captchaImage(captcha.toBase64())
        .build();
  }

  @Override
  public void verifyCaptcha(String captchaToken, String captchaCode) {
    if (captchaToken == null || captchaToken.isBlank() || captchaCode == null || captchaCode.isBlank()) {
      throw new ClientException("A000600", "验证码不能为空");
    }
    String key = RedisKeyConstants.captchaKey(captchaToken);
    String stored = stringRedisTemplate.opsForValue().getAndDelete(key);
    if (stored == null) {
      throw new ClientException("A000601", "验证码已过期");
    }
    if (!stored.equals(captchaCode.toLowerCase())) {
      throw new ClientException("A000602", "验证码错误");
    }
  }
}