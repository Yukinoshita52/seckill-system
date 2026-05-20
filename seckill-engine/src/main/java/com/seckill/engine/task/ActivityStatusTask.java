package com.seckill.engine.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seckill.common.dao.entity.SeckillActivityDO;
import com.seckill.common.dao.mapper.SeckillActivityMapper;
import com.seckill.engine.cache.ActivityCacheService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 活动状态定时同步任务。
 *
 * <p>扫描 DB 中状态与实际时间不符的活动，自动修正：
 * <ul>
 *   <li>status=0 且 start_time <= now → 更新为 1（进行中）</li>
 *   <li>status=1 且 end_time < now → 更新为 2（已结束）</li>
 * </ul>
 * <p>同时同步 Redis 缓存中的 status 字段。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivityStatusTask {

  private final SeckillActivityMapper activityMapper;
  private final ActivityCacheService activityCacheService;

  @Scheduled(fixedDelayString = "${seckill.activity.status-scan-interval:60000}")
  public void syncActivityStatus() {
    LocalDateTime now = LocalDateTime.now();

    // 未开始 → 进行中
    List<SeckillActivityDO> toStart = activityMapper.selectList(
        new LambdaQueryWrapper<SeckillActivityDO>()
            .eq(SeckillActivityDO::getStatus, 0)
            .le(SeckillActivityDO::getStartTime, now)
            .select(SeckillActivityDO::getId));

    for (SeckillActivityDO activity : toStart) {
      int affected = activityMapper.update(null,
          new LambdaUpdateWrapper<SeckillActivityDO>()
              .eq(SeckillActivityDO::getId, activity.getId())
              .eq(SeckillActivityDO::getStatus, 0)
              .set(SeckillActivityDO::getStatus, 1));
      if (affected > 0) {
        syncCache(activity.getId());
        log.info("活动状态更新: id={} 0→1(进行中)", activity.getId());
      }
    }

    // 进行中 → 已结束
    List<SeckillActivityDO> toEnd = activityMapper.selectList(
        new LambdaQueryWrapper<SeckillActivityDO>()
            .eq(SeckillActivityDO::getStatus, 1)
            .lt(SeckillActivityDO::getEndTime, now)
            .select(SeckillActivityDO::getId));

    for (SeckillActivityDO activity : toEnd) {
      int affected = activityMapper.update(null,
          new LambdaUpdateWrapper<SeckillActivityDO>()
              .eq(SeckillActivityDO::getId, activity.getId())
              .eq(SeckillActivityDO::getStatus, 1)
              .set(SeckillActivityDO::getStatus, 2));
      if (affected > 0) {
        syncCache(activity.getId());
        log.info("活动状态更新: id={} 1→2(已结束)", activity.getId());
      }
    }

    if (!toStart.isEmpty() || !toEnd.isEmpty()) {
      log.info("活动状态同步完成: 启动={}, 结束={}", toStart.size(), toEnd.size());
    }
  }

  private void syncCache(Long activityId) {
    try {
      activityCacheService.initActivityCache(activityId);
    } catch (Exception e) {
      log.warn("活动缓存同步失败: activityId={}, reason={}", activityId, e.getMessage());
    }
  }
}
