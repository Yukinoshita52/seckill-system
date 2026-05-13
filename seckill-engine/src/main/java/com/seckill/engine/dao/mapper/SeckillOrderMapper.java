package com.seckill.engine.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.engine.dao.entity.SeckillOrderDO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SeckillOrderMapper extends BaseMapper<SeckillOrderDO> {
  // todo: 此方法供订单超时关单定时任务使用，待实现 ScheduledTask / Job
  @Select(
      "SELECT * FROM t_seckill_order WHERE status = #{status} AND create_time < #{beforeTime} AND is_deleted = 0")
  List<SeckillOrderDO> selectByStatusAndCreateTime(
      @Param("status") Integer status, @Param("beforeTime") String beforeTime);
}
