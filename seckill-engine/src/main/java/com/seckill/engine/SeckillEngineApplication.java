package com.seckill.engine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;

@Slf4j
@SpringBootApplication(
    exclude = {
      DataSourceAutoConfiguration.class,
      RedisAutoConfiguration.class,
      RedisRepositoriesAutoConfiguration.class
    })
public class SeckillEngineApplication {

  private final Environment env;

  public SeckillEngineApplication(Environment env) {
    this.env = env;
  }

  public static void main(String[] args) {
    SpringApplication.run(SeckillEngineApplication.class, args);
  }

  @EventListener(ApplicationReadyEvent.class)
  public void onStartup() {
    String port = env.getProperty("server.port", "8080");
    String contextPath = env.getProperty("server.servlet.context-path", "");
    log.info("========================================");
    log.info("项目启动成功！");
    log.info("应用地址: http://localhost:{}{}", port, contextPath);
    log.info("Knife4j 文档: http://localhost:{}{}/doc.html", port, contextPath);
    log.info("========================================");
  }
}
