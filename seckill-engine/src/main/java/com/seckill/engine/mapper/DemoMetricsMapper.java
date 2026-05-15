package com.seckill.engine.mapper;

import com.seckill.engine.dto.resp.DemoStatusCountRespDTO;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DemoMetricsMapper {

  @Select("""
      SELECT
        CASE status
          WHEN 0 THEN 'PENDING'
          WHEN 1 THEN 'UNPAID'
          WHEN 2 THEN 'SUCCESS'
          WHEN 3 THEN 'FAILED'
          WHEN 4 THEN 'TIMEOUT'
          ELSE 'UNKNOWN'
        END AS status,
        COUNT(*) AS count
      FROM t_seckill_order
      WHERE activity_id = #{activityId} AND is_deleted = 0
      GROUP BY status
      ORDER BY status
      """)
  List<DemoStatusCountRespDTO> selectStatusCountsByActivityId(@Param("activityId") Long activityId);
}
