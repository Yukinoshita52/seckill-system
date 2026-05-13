package com.seckill.engine.service.chain;

import com.seckill.framework.exception.ClientException;
import org.springframework.stereotype.Component;

/** 参数校验处理器 */
@Component
public class SeckillParamCheckHandler implements SeckillChainHandler {

  @Override
  public void handle(SeckillChainContext context) {
    if (context.getReq().getActivityId() == null) {
      throw new ClientException("A000100", "活动ID不能为空");
    }
  }

  @Override
  public String name() {
    return "ParamCheck";
  }
}
