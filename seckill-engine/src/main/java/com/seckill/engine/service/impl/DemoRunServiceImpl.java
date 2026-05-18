package com.seckill.engine.service.impl;

import com.seckill.engine.dto.req.DemoRunReqDTO;
import com.seckill.engine.dto.req.SeckillOrderReqDTO;
import com.seckill.engine.dto.resp.DemoRunRespDTO;
import com.seckill.engine.service.DemoRunService;
import com.seckill.engine.service.SeckillService;
import com.seckill.framework.exception.ClientException;
import com.seckill.framework.toolkit.UserContext;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DemoRunServiceImpl implements DemoRunService {

  private static final int MAX_REQUEST_COUNT = 1000;
  private static final int DEMO_USER_BASE = 900000;

  private final SeckillService seckillService;

  @Override
  public DemoRunRespDTO runDemo(DemoRunReqDTO req) {
    Long activityId = req.getActivityId();
    Integer reqCount = req.getRequestCount();
    if (activityId == null) {
      throw new ClientException("A000100", "活动ID不能为空");
    }
    if (reqCount == null || reqCount <= 0) {
      throw new ClientException("A000100", "模拟请求数必须大于 0");
    }
    if (reqCount > MAX_REQUEST_COUNT) {
      throw new ClientException("A000100", "模拟请求数不能超过 " + MAX_REQUEST_COUNT);
    }

    int concurrency = Math.min(reqCount, 32);
    ExecutorService executor = Executors.newFixedThreadPool(concurrency);
    List<Callable<Boolean>> tasks = new ArrayList<>(reqCount);
    for (int i = 0; i < reqCount; i++) {
      final long demoUserId = DEMO_USER_BASE + i + System.nanoTime() % 100000;
      tasks.add(() -> submitDemoOrder(activityId, demoUserId));
    }

    int acceptedCount = 0;
    try {
      List<Future<Boolean>> futures = executor.invokeAll(tasks);
      for (Future<Boolean> future : futures) {
        if (Boolean.TRUE.equals(future.get())) {
          acceptedCount++;
        }
      }
    } catch (Exception ex) {
      Thread.currentThread().interrupt();
      throw new ClientException("B000100", "演示触发失败，请稍后重试");
    } finally {
      executor.shutdown();
    }

    int rejectedCount = reqCount - acceptedCount;
    return DemoRunRespDTO.builder()
        .activityId(activityId)
        .requestCount(reqCount)
        .acceptedCount(acceptedCount)
        .rejectedCount(rejectedCount)
        .message("已触发 " + reqCount + " 次演示请求，受理 " + acceptedCount + " 次")
        .build();
  }

  private boolean submitDemoOrder(Long activityId, Long demoUserId) {
    try {
      UserContext.setUserId(demoUserId);
      UserContext.setUsername("demo-" + demoUserId);
      SeckillOrderReqDTO orderReq = new SeckillOrderReqDTO();
      orderReq.setActivityId(activityId);
      seckillService.placeOrder(orderReq);
      return true;
    } catch (Exception ignored) {
      return false;
    } finally {
      UserContext.clear();
    }
  }
}
