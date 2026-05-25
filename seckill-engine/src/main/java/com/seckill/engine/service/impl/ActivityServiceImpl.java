package com.seckill.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.engine.dao.entity.SeckillActivityDO;
import com.seckill.engine.dao.mapper.SeckillActivityMapper;
import com.seckill.engine.cache.ActivityCacheService;
import com.seckill.engine.dto.resp.ActivityQueryRespDTO;
import com.seckill.engine.service.ActivityService;
import com.seckill.engine.service.StockService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActivityServiceImpl implements ActivityService {

  private final StockService stockService;
  private final ActivityCacheService activityCacheService;
  private final SeckillActivityMapper activityMapper;

  @Override
  public List<ActivityQueryRespDTO> listActivities() {
    // 只查 ID 列表（轻量查询），详情从 Redis 读取
    List<Long> ids = activityMapper.selectList(
        new LambdaQueryWrapper<SeckillActivityDO>().select(SeckillActivityDO::getId))
        .stream().map(SeckillActivityDO::getId).toList();

    List<ActivityQueryRespDTO> result = new ArrayList<>();
    for (Long id : ids) {
      SeckillActivityDO activity = activityCacheService.getFromRedis(id);
      if (activity == null) {
        continue; // 缓存未命中，跳过
      }
      long remainStock = stockService.getTotalStock(id);
      result.add(ActivityQueryRespDTO.builder()
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
          .build());
    }
    return result;
  }

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
