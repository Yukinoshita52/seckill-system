package com.seckill.engine.service.impl;

import com.seckill.engine.dao.entity.SeckillActivityDO;
import com.seckill.engine.dao.mapper.SeckillActivityMapper;
import com.seckill.engine.dto.resp.ActivityQueryRespDTO;
import com.seckill.engine.service.ActivityService;
import com.seckill.engine.service.StockService;
import com.seckill.framework.exception.ClientException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

  private final SeckillActivityMapper activityMapper;
  private final StockService stockService;

  @Override
  public ActivityQueryRespDTO queryActivity(Long id) {
    // todo: 活动详情应缓存到 Redis（如 hash 结构），高频读取场景避免每次查库
    SeckillActivityDO activity = activityMapper.selectById(id);
    if (activity == null) {
      throw new ClientException("A000100", "活动不存在");
    }

    long remainStock = stockService.getTotalStock(id);

    return ActivityQueryRespDTO.builder()
        .id(activity.getId())
        .activityName(activity.getActivityName())
        .goodsName(activity.getGoodsName())
        .originalPrice(activity.getOriginalPrice())
        .seckillPrice(activity.getSeckillPrice())
        .totalStock(activity.getTotalStock())
        .remainStock(remainStock)
        .startTime(activity.getStartTime())
        .endTime(activity.getEndTime())
        .status(resolveStatus(activity))
        .build();
  }

  // todo: 活动状态应缓存（短 TTL），避免每次请求重新计算；状态变更时主动刷新缓存
  private int resolveStatus(SeckillActivityDO activity) {
    LocalDateTime now = LocalDateTime.now();
    if (now.isBefore(activity.getStartTime())) {
      return 0; // 未开始
    }
    if (now.isAfter(activity.getEndTime())) {
      return 2; // 已结束
    }
    return 1; // 进行中
  }
}
