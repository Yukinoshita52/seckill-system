package com.seckill.engine.controller;

import com.seckill.engine.dto.resp.OrderStatusRespDTO;
import com.seckill.engine.service.OrderService;
import com.seckill.framework.result.Result;
import com.seckill.framework.result.Results;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "订单")
@RestController
@RequestMapping("/api/order")
@RequiredArgsConstructor
public class OrderController {

  private final OrderService orderService;

  @Operation(summary = "查询订单状态")
  @GetMapping("/status/{orderNo}")
  public Result<OrderStatusRespDTO> queryOrderStatus(@PathVariable String orderNo) {
    return Results.success(orderService.queryOrderStatus(orderNo));
  }
}
