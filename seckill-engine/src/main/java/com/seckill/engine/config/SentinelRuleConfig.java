package com.seckill.engine.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowItem;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Sentinel 限流规则初始化 */
@Slf4j
@Component
public class SentinelRuleConfig {

  @PostConstruct
  public void initRules() {
    initFlowRules();
    initParamFlowRules();
  }

  /** 普通流控规则 — 秒杀接口整体 QPS 限流 */
  private void initFlowRules() {
    List<FlowRule> rules = new ArrayList<>();

    FlowRule seckillOrder = new FlowRule();
    seckillOrder.setResource("seckill-order");
    seckillOrder.setGrade(RuleConstant.FLOW_GRADE_QPS);
    seckillOrder.setCount(100);
    seckillOrder.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
    seckillOrder.setLimitApp("default");
    rules.add(seckillOrder);

    // todo: 可通过 Sentinel Dashboard 动态调整规则，此处仅为兜底默认值
    // com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager.loadRules(rules);
    log.info("Sentinel 流控规则已定义（默认未加载，通过 Dashboard 配置生效）: {}", rules);
  }

  /** 热点参数流控规则 — 按 activityId 限流 */
  private void initParamFlowRules() {
    List<ParamFlowRule> rules = new ArrayList<>();

    ParamFlowRule activityParamRule = new ParamFlowRule();
    activityParamRule.setResource("seckill-order");
    activityParamRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
    activityParamRule.setParamIdx(0);
    activityParamRule.setCount(50);

    ParamFlowItem item = new ParamFlowItem();
    item.setClassType(Long.class.getTypeName());
    activityParamRule.setParamFlowItemList(List.of(item));

    rules.add(activityParamRule);

    // todo: 热点参数规则暂未加载，建议通过 Dashboard 动态配置
    // com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager.loadRules(rules);
    log.info("Sentinel 热点参数规则已定义（默认未加载，通过 Dashboard 配置生效）: {}", rules);
  }
}
