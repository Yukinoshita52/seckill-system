package com.seckill.engine.service.impl;

import com.seckill.common.dao.entity.SeckillActivityDO;
import com.seckill.common.dao.mapper.SeckillActivityMapper;
import com.seckill.engine.dto.resp.DemoMetricsRespDTO;
import com.seckill.engine.dto.resp.DemoStatusCountRespDTO;
import com.seckill.engine.mapper.DemoMetricsMapper;
import com.seckill.engine.service.DemoMetricsService;
import com.seckill.engine.service.StockService;
import com.seckill.framework.exception.ClientException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DemoMetricsServiceImpl implements DemoMetricsService {

  private final DemoMetricsMapper demoMetricsMapper;
  private final SeckillActivityMapper activityMapper;
  private final StockService stockService;

  @Override
  public DemoMetricsRespDTO queryDemoMetrics(Long activityId) {
    SeckillActivityDO activity = activityMapper.selectById(activityId);
    if (activity == null) {
      throw new ClientException("A000100", "活动不存在");
    }

    List<DemoStatusCountRespDTO> statusFlow =
        demoMetricsMapper.selectStatusCountsByActivityId(activityId);

    long pendingCount = countByStatus(statusFlow, "PENDING");
    long unpaidCount = countByStatus(statusFlow, "UNPAID");
    long successPaidCount = countByStatus(statusFlow, "SUCCESS");
    long failedCount = countByStatus(statusFlow, "FAILED");
    long timeoutCount = countByStatus(statusFlow, "TIMEOUT");
    long requestCount = statusFlow.stream().mapToLong(item -> defaultCount(item.getCount())).sum();

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

  private long countByStatus(List<DemoStatusCountRespDTO> statusFlow, String status) {
    return statusFlow.stream()
        .filter(item -> status.equals(item.getStatus()))
        .mapToLong(item -> defaultCount(item.getCount()))
        .sum();
  }

  private long defaultCount(Long count) {
    return count != null ? count : 0L;
  }
}
