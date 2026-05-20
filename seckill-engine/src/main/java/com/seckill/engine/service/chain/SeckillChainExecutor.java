package com.seckill.engine.service.chain;

import com.seckill.engine.dto.req.SeckillOrderReqDTO;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** 责任链执行器 — 按顺序执行各处理器 */
@Slf4j
@Component
public class SeckillChainExecutor {

  private final List<SeckillChainHandler> handlers;

  // 按照构造顺序依次进行校验
  public SeckillChainExecutor(
      SeckillParamCheckHandler paramCheck,
      ActivityStatusCheckHandler activityStatusCheck,
      UserRepeatRequestCheckHandler userRepeatRequestCheck,
      UserRepeatBoughtCheckHandler userRepeatBoughtCheck,
      StockPreCheckHandler stockPreCheck) {
    this.handlers = List.of(paramCheck, activityStatusCheck, userRepeatRequestCheck, userRepeatBoughtCheck, stockPreCheck);
  }

  /** 执行责任链校验，返回携带活动信息的上下文 */
  public SeckillChainContext execute(SeckillOrderReqDTO req) {
    SeckillChainContext context = new SeckillChainContext(req);
    for (SeckillChainHandler handler : handlers) {
      log.debug("执行责任链处理器: {}", handler.name());
      handler.handle(context);
    }
    return context;
  }
}
