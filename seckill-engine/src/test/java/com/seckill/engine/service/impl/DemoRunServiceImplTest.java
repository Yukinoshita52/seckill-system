package com.seckill.engine.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.seckill.engine.dto.req.DemoRunReqDTO;
import com.seckill.engine.dto.req.SeckillOrderReqDTO;
import com.seckill.engine.dto.resp.DemoRunRespDTO;
import com.seckill.engine.dto.resp.SeckillOrderRespDTO;
import com.seckill.engine.service.SeckillService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DemoRunServiceImplTest {

  @Mock private SeckillService seckillService;

  @InjectMocks private DemoRunServiceImpl demoRunService;

  @Test
  void shouldRunDemoRequestsAndReturnAcceptedSummary() {
    when(seckillService.placeOrder(any(SeckillOrderReqDTO.class)))
        .thenReturn(SeckillOrderRespDTO.builder().orderNo("SK-DEMO").status("PENDING").message("排队中").build());

    DemoRunRespDTO result =
        demoRunService.runDemo(DemoRunReqDTO.builder().activityId(1L).requestCount(5).build());

    assertThat(result.getActivityId()).isEqualTo(1L);
    assertThat(result.getRequestCount()).isEqualTo(5);
    assertThat(result.getAcceptedCount()).isEqualTo(5);
    assertThat(result.getRejectedCount()).isEqualTo(0);
    assertThat(result.getMessage()).contains("5");
  }
}
