package com.seckill.engine.task;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.seckill.engine.cache.ActivityCacheService;
import com.seckill.engine.dao.entity.SeckillActivityDO;
import com.seckill.engine.dao.entity.SeckillOrderDO;
import com.seckill.engine.dao.mapper.SeckillActivityMapper;
import com.seckill.engine.dao.mapper.SeckillOrderMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 售罄判定定时任务。
 *
 * <p>每10分钟扫描进行中的活动，查询已支付订单数量是否等于总库存。
 * 如果一致，说明确实售罄，更新 Redis 中对应活动的 status 为 2（已售罄）。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SoldOutCheckTask {

  private final SeckillActivityMapper activityMapper;
  private final SeckillOrderMapper orderMapper;
  private final ActivityCacheService activityCacheService;

  private static final int STATUS_ONGOING = 1;
  private static final int STATUS_SOLD_OUT = 2;
  private static final int STATUS_PAID = 2;

  @Scheduled(fixedDelayString = "${seckill.soldout-scan-interval:600000}")
    public void checkSoldOut() {
    List<SeckillActivityDO> activities = activityMapper.selectList(
        new LambdaQueryWrapper<SeckillActivityDO>()
            .eq(SeckillActivityDO::getStatus, STATUS_ONGOING)
            .select(SeckillActivityDO::getId, SeckillActivityDO::getTotalStock));

    if (activities.isEmpty()) {
      return;
    }

    for (SeckillActivityDO activity : activities) {
      Long paidCount = orderMapper.selectCount(
          new LambdaQueryWrapper<SeckillOrderDO>()
              .eq(SeckillOrderDO::getActivityId, activity.getId())
              .eq(SeckillOrderDO::getStatus, STATUS_PAID));

      if (paidCount != null && paidCount >= activity.getTotalStock()) {
        log.info("活动已售罄: activityId={}, paidCount={}, totalStock={}",
            activity.getId(), paidCount, activity.getTotalStock());
        activityCacheService.updateStatusInRedis(activity.getId(), STATUS_SOLD_OUT);
        activityMapper.update(null,
            new LambdaUpdateWrapper<SeckillActivityDO>()
                .eq(SeckillActivityDO::getId, activity.getId())
                .eq(SeckillActivityDO::getStatus, STATUS_ONGOING)
                .set(SeckillActivityDO::getStatus, STATUS_SOLD_OUT));
      }
    }
  }
}
