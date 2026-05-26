package com.seckill.engine.service.chain;

import com.seckill.engine.service.CaptchaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CaptchaCheckHandler implements SeckillChainHandler {

  private final CaptchaService captchaService;

  @Override
  public void handle(SeckillChainContext context) {
    captchaService.verifyCaptcha(context.getReq().getCaptchaToken(), context.getReq().getCaptchaCode());
  }

  @Override
  public String name() {
    return "CaptchaCheck";
  }
}