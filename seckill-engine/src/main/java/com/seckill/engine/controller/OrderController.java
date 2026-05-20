package com.seckill.engine.controller;

import com.seckill.engine.dto.resp.OrderStatusRespDTO;
import com.seckill.engine.service.OrderService;
import com.seckill.framework.result.Result;
import com.seckill.framework.result.Results;
import com.seckill.framework.toolkit.UserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

  @Operation(summary = "支付订单")
  @PostMapping("/pay/{orderNo}")
  public Result<Void> payOrder(@PathVariable String orderNo) {
    orderService.payOrder(orderNo, UserContext.getUserId());
    return Results.success();
  }

  @Operation(summary = "取消订单")
  @PostMapping("/cancel/{orderNo}")
  public Result<Void> cancelOrder(@PathVariable String orderNo) {
    orderService.cancelOrder(orderNo, UserContext.getUserId());
    return Results.success();
  }

  @Operation(summary = "我的订单列表")
  @GetMapping("/list")
  public Result<List<OrderStatusRespDTO>> listOrders() {
    return Results.success(orderService.listByUser(UserContext.getUserId()));
  }
}
