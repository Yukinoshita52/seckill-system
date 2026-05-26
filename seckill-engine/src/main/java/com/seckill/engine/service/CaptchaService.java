package com.seckill.engine.service;

import com.seckill.engine.dto.resp.CaptchaRespDTO;

public interface CaptchaService {

  CaptchaRespDTO generateCaptcha();

  void verifyCaptcha(String captchaToken, String captchaCode);
}