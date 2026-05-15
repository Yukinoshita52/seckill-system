package com.seckill.engine.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.seckill.common.dao.entity.SeckillActivityDO;
import com.seckill.common.dao.mapper.SeckillActivityMapper;
import com.seckill.engine.dto.resp.DemoMetricsRespDTO;
import com.seckill.engine.dto.resp.DemoStatusCountRespDTO;
import com.seckill.engine.mapper.DemoMetricsMapper;
import com.seckill.engine.service.StockService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DemoMetricsServiceImplTest {

  @Mock private DemoMetricsMapper demoMetricsMapper;
  @Mock private SeckillActivityMapper activityMapper;
  @Mock private StockService stockService;

  @InjectMocks private DemoMetricsServiceImpl demoMetricsService;

  @Test
  void shouldAggregateRealDemoMetrics() {
    Long activityId = 1L;
    when(activityMapper.selectById(activityId))
        .thenReturn(SeckillActivityDO.builder()
            .id(activityId)
            .activityName("iPhone 16 限时秒杀")
            .totalStock(1000)
            .build());
    when(stockService.getTotalStock(activityId)).thenReturn(418L);
    when(demoMetricsMapper.selectStatusCountsByActivityId(activityId))
        .thenReturn(List.of(
            new DemoStatusCountRespDTO("PENDING", 7L),
            new DemoStatusCountRespDTO("UNPAID", 39L),
            new DemoStatusCountRespDTO("FAILED", 82L),
            new DemoStatusCountRespDTO("SUCCESS", 5L),
            new DemoStatusCountRespDTO("TIMEOUT", 3L)));

    DemoMetricsRespDTO result = demoMetricsService.queryDemoMetrics(activityId);

    assertThat(result.getActivityId()).isEqualTo(activityId);
    assertThat(result.getActivityName()).isEqualTo("iPhone 16 限时秒杀");
    assertThat(result.getRequestCount()).isEqualTo(136L);
    assertThat(result.getPendingCount()).isEqualTo(7L);
    assertThat(result.getUnpaidCount()).isEqualTo(39L);
    assertThat(result.getSuccessCount()).isEqualTo(44L);
    assertThat(result.getFailedCount()).isEqualTo(82L);
    assertThat(result.getTimeoutCount()).isEqualTo(3L);
    assertThat(result.getRemainStock()).isEqualTo(418L);
    assertThat(result.getTotalStock()).isEqualTo(1000);
    assertThat(result.getStatusFlow())
        .extracting(DemoStatusCountRespDTO::getStatus, DemoStatusCountRespDTO::getCount)
        .containsExactly(
            org.assertj.core.groups.Tuple.tuple("PENDING", 7L),
            org.assertj.core.groups.Tuple.tuple("UNPAID", 39L),
            org.assertj.core.groups.Tuple.tuple("FAILED", 82L),
            org.assertj.core.groups.Tuple.tuple("SUCCESS", 5L),
            org.assertj.core.groups.Tuple.tuple("TIMEOUT", 3L));
  }
}
