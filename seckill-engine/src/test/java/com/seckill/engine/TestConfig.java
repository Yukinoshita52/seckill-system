package com.seckill.engine;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.mockito.Mockito;

@TestConfiguration
public class TestConfig {

  @Bean
  public RocketMQTemplate rocketMQTemplate() {
    return Mockito.mock(RocketMQTemplate.class);
  }
}
