package com.seckill.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.engine.dao.entity.SeckillActivityDO;
import com.seckill.engine.dao.mapper.SeckillActivityMapper;
import com.seckill.engine.cache.ActivityCacheService;
import com.seckill.engine.dto.resp.ActivityQueryRespDTO;
import com.seckill.engine.dto.resp.SoldOutCheckRespDTO;
import com.seckill.engine.service.ActivityService;
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

  private final ActivityCacheService activityCacheService;
  private final SeckillActivityMapper activityMapper;

  @Override
  public List<ActivityQueryRespDTO> listActivities() {
    List<Long> ids = activityMapper.selectList(
        new LambdaQueryWrapper<SeckillActivityDO>().select(SeckillActivityDO::getId))
        .stream().map(SeckillActivityDO::getId).toList();

    List<ActivityQueryRespDTO> result = new ArrayList<>();
    for (Long id : ids) {
      SeckillActivityDO activity = activityCacheService.getFromRedis(id);
      if (activity == null) {
        continue;
      }
      result.add(ActivityQueryRespDTO.builder()
          .id(activity.getId())
          .activityName(activity.getActivityName())
          .goodsName(activity.getGoodsName())
          .originalPrice(activity.getOriginalPrice())
          .seckillPrice(activity.getSeckillPrice())
          .totalStock(activity.getTotalStock())
          .soldOut(resolveStatus(activity) == 2)
          .startTime(activity.getStartTime())
          .endTime(activity.getEndTime())
          .status(resolveStatus(activity))
          .build());
    }
    return result;
  }

  @Override
  public SoldOutCheckRespDTO checkSoldOut(Long activityId) {
    SeckillActivityDO activity = activityCacheService.getFromRedis(activityId);
    if (activity == null) {
      return SoldOutCheckRespDTO.builder()
          .activityId(activityId)
          .soldOut(false)
          .build();
    }
    boolean soldOut = activity.getStatus() != null && activity.getStatus() == 2;
    return SoldOutCheckRespDTO.builder()
        .activityId(activityId)
        .soldOut(soldOut)
        .build();
  }

  private int resolveStatus(SeckillActivityDO activity) {
    LocalDateTime now = LocalDateTime.now();
    if (now.isBefore(activity.getStartTime())) {
      return 0;
    }
    if (now.isAfter(activity.getEndTime())) {
      return 3;
    }
    return activity.getStatus();
  }
}