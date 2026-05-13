package com.seckill.engine.controller;

import com.seckill.engine.dto.req.SeckillOrderReqDTO;
import com.seckill.engine.dto.resp.SeckillOrderRespDTO;
import com.seckill.engine.service.SeckillService;
import com.seckill.framework.result.Result;
import com.seckill.framework.result.Results;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
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

  // todo: 接入 Sentinel 热点参数限流或网关层限流，防刷防超卖
  // todo: 新增 GET /api/seckill/order/{orderNo} 接口，返回 OrderStatusRespDTO，供前端轮询
  @Operation(summary = "秒杀下单")
  @PostMapping("/order")
  public Result<SeckillOrderRespDTO> placeOrder(@RequestBody SeckillOrderReqDTO req) {
    return Results.success(seckillService.placeOrder(req));
  }
}
