package com.seckill.engine.service.impl;

import com.seckill.engine.dao.entity.SeckillActivityDO;
import com.seckill.engine.cache.ActivityCacheService;
import com.seckill.engine.dto.resp.DemoMetricsRespDTO;
import com.seckill.engine.dto.resp.DemoStatusCountRespDTO;
import com.seckill.engine.mapper.DemoMetricsMapper;
import com.seckill.engine.service.DemoMetricsService;
import com.seckill.engine.service.StockService;
import com.seckill.framework.exception.ClientException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DemoMetricsServiceImpl implements DemoMetricsService {

  private final DemoMetricsMapper demoMetricsMapper;
  private final ActivityCacheService activityCacheService;
  private final StockService stockService;

  @Override
  public DemoMetricsRespDTO queryDemoMetrics(Long activityId) {
    SeckillActivityDO activity = activityCacheService.getWithProtection(activityId);
    if (activity == null) {
      throw new ClientException("A000100", "活动不存在");
    }

    List<DemoStatusCountRespDTO> statusFlow =
        demoMetricsMapper.selectStatusCountsByActivityId(activityId);

    Map<String, Long> countMap = statusFlow.stream()
        .collect(Collectors.toMap(
            DemoStatusCountRespDTO::getStatus,
            item -> item.getCount() != null ? item.getCount() : 0L
        ));

    long pendingCount = countMap.getOrDefault("PENDING", 0L);
    long unpaidCount = countMap.getOrDefault("UNPAID", 0L);
    long successPaidCount = countMap.getOrDefault("SUCCESS", 0L);
    long failedCount = countMap.getOrDefault("FAILED", 0L);
    long timeoutCount = countMap.getOrDefault("TIMEOUT", 0L);
    long requestCount = countMap.values().stream().mapToLong(Long::longValue).sum();

    return DemoMetricsRespDTO.builder()
        .activityId(activity.getId())
        .activityName(activity.getActivityName())
        .requestCount(requestCount)
        .pendingCount(pendingCount)
        .unpaidCount(unpaidCount)
        .successCount(unpaidCount + successPaidCount)
        .failedCount(failedCount)
        .timeoutCount(timeoutCount)
        .remainStock(stockService.getTotalStock(activityId))
        .totalStock(activity.getTotalStock())
        .statusFlow(statusFlow)
        .build();
  }
}
