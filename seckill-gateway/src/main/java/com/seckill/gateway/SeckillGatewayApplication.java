package com.seckill.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication
@EnableDiscoveryClient
public class SeckillGatewayApplication {

  private final Environment env;

  public SeckillGatewayApplication(Environment env) {
    this.env = env;
  }

  public static void main(String[] args) {
    SpringApplication.run(SeckillGatewayApplication.class, args);
  }

  @EventListener(ApplicationReadyEvent.class)
  public void onStartup() {
    String port = env.getProperty("server.port", "8080");
    log.info("========================================");
    log.info("网关启动成功！");
    log.info("网关地址: http://localhost:{}", port);
    log.info("========================================");
  }
}
