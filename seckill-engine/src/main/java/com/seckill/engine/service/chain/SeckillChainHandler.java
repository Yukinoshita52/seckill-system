package com.seckill.engine.service.chain;

/** 责任链处理器接口 */
public interface SeckillChainHandler {

  /** 执行校验，抛出异常表示校验失败 */
  void handle(SeckillChainContext context);

  /** 处理器名称，用于日志 */
  String name();
}
