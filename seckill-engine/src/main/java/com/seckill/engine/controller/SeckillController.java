package com.seckill.engine.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.seckill.engine.dto.req.SeckillOrderReqDTO;
import com.seckill.engine.dto.resp.CaptchaRespDTO;
import com.seckill.engine.dto.resp.SeckillOrderRespDTO;
import com.seckill.engine.service.CaptchaService;
import com.seckill.engine.service.SeckillService;
import com.seckill.framework.result.Result;
import com.seckill.framework.result.Results;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "秒杀")
@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillController {

  private final SeckillService seckillService;
  private final CaptchaService captchaService;

  // todo: 新增 GET /api/seckill/order/{orderNo} 接口，返回 OrderStatusRespDTO，供前端轮询

  @Operation(summary = "获取验证码")
  @GetMapping("/captcha")
  public Result<CaptchaRespDTO> getCaptcha() {
    return Results.success(captchaService.generateCaptcha());
  }

  @Operation(summary = "秒杀下单")
  @PostMapping("/order")
  @SentinelResource(value = "seckill-order", blockHandler = "placeOrderBlockHandler")
  public Result<SeckillOrderRespDTO> placeOrder(@RequestBody SeckillOrderReqDTO req) {
    return Results.success(seckillService.placeOrder(req));
  }

  public Result<SeckillOrderRespDTO> placeOrderBlockHandler(SeckillOrderReqDTO req, BlockException e) {
    return Results.failure("B000300", "系统繁忙，请稍后重试");
  }
}
