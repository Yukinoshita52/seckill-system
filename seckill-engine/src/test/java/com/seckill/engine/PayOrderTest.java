package com.seckill.engine;

import com.seckill.engine.dao.entity.SeckillOrderDO;
import com.seckill.engine.dao.mapper.SeckillOrderMapper;
import com.seckill.engine.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
@Import(TestConfig.class)
public class PayOrderTest {

  @Autowired private OrderService orderService;
  @Autowired private SeckillOrderMapper orderMapper;

  @Test
  public void payAllOrders(){
    payAllOrders(13L);
  }

  /**
   * 批量支付指定活动的所有待支付订单（伪调用，跳过 UserContext）。
   *
   * @param activityId 活动ID
   */
  @Test
  public void payAllOrders(Long activityId) {
    var orders = orderMapper.selectList(
        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SeckillOrderDO>()
            .eq(SeckillOrderDO::getActivityId, activityId)
            .eq(SeckillOrderDO::getStatus, 1));

    log.info("待支付订单数: {}", orders.size());
    for (SeckillOrderDO order : orders) {
      try {
        orderService.payOrder(order.getOrderNo(), order.getUserId());
        log.info("支付成功: orderNo={}, userId={}", order.getOrderNo(), order.getUserId());
      } catch (Exception e) {
        log.error("支付失败: orderNo={}, userId={}, reason={}",
            order.getOrderNo(), order.getUserId(), e.getMessage());
      }
    }
  }

  @Test
  public void paySingleOrder() {
    // TODO: 指定 orderNo 和 userId 后直接调用
//     orderService.payOrder("SK20260525120000123456", 12345L);
  }
}
