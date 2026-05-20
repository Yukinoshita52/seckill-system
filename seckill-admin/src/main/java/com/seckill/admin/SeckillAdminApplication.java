package com.seckill.admin;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication
@ComponentScan(basePackages = {"com.seckill.admin", "com.seckill.framework"})
@MapperScan("com.seckill.admin.dao.mapper")
public class SeckillAdminApplication {

  private final Environment env;

  public SeckillAdminApplication(Environment env) {
    this.env = env;
  }

  public static void main(String[] args) {
    SpringApplication.run(SeckillAdminApplication.class, args);
  }

  @EventListener(ApplicationReadyEvent.class)
  public void onStartup() {
    String port = env.getProperty("server.port", "8080");
    log.info("========================================");
    log.info("管理后台启动成功！");
    log.info("应用地址: http://localhost:{}", port);
    log.info("Knife4j 文档: http://localhost:{}/doc.html", port);
    log.info("========================================");
  }
}
