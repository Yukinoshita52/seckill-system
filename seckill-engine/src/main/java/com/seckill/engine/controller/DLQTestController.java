package com.seckill.engine.controller;

import com.seckill.engine.dao.entity.StockChangeLogDO;
import com.seckill.engine.dao.mapper.StockChangeLogMapper;
import com.seckill.engine.mq.OrderMessage;
import com.seckill.engine.mq.producer.SeckillOrderProducer;
import com.seckill.framework.result.Result;
import com.seckill.framework.result.Results;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

@Tag(name = "DLQ测试")
@RestController
@RequestMapping("/api/test/dlq")
@RequiredArgsConstructor
public class DLQTestController {

  private final StockChangeLogMapper stockChangeLogMapper;
  private final SeckillOrderProducer orderProducer;

  /** 控制主消费者是否模拟消费失败 */
  public static final AtomicBoolean FORCE_FAIL = new AtomicBoolean(false);

  @Operation(summary = "开启/关闭消费者模拟失败（开启后主消费者会抛异常，消息进入 DLQ）")
  @PostMapping("/toggle-fail")
  public Result<String> toggleFail() {
    boolean newVal = !FORCE_FAIL.get();
    FORCE_FAIL.set(newVal);
    return Results.success("模拟失败开关: " + (newVal ? "开启" : "关闭"));
  }

  @Operation(summary = "预插入 change_log（触发主消费者唯一键冲突）")
  @PostMapping("/pre-insert")
  public Result<String> preInsert(
      @RequestParam String orderNo,
      @RequestParam Long activityId,
      @RequestParam Long userId,
      @RequestParam(defaultValue = "0") Integer bucketIndex) {
    StockChangeLogDO log = StockChangeLogDO.builder()
        .activityId(activityId)
        .userId(userId)
        .orderNo(orderNo)
        .changeType(0)
        .changeQuantity(1)
        .bucketIndex(bucketIndex)
        .changeTime(LocalDateTime.now())
        .build();
    stockChangeLogMapper.insert(log);
    return Results.success("预插入成功: " + orderNo);
  }

  @Operation(summary = "发送订单消息（配合预插入触发 DLQ）")
  @PostMapping("/send-message")
  public Result<String> sendMessage(
      @RequestParam String orderNo,
      @RequestParam Long activityId,
      @RequestParam Long userId,
      @RequestParam(defaultValue = "0") Integer bucketIndex,
      @RequestParam(defaultValue = "99.00") String seckillPrice) {
    OrderMessage message = OrderMessage.builder()
        .orderNo(orderNo)
        .activityId(activityId)
        .userId(userId)
        .bucketIndex(bucketIndex)
        .seckillPrice(new java.math.BigDecimal(seckillPrice))
        .build();
    orderProducer.send(message);
    return Results.success("消息发送成功: " + orderNo);
  }
}
