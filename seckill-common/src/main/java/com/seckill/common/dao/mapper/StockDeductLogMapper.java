package com.seckill.common.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.common.dao.entity.StockDeductLogDO;
import org.apache.ibatis.annotations.Mapper;

// todo: 待接入库存扣减审计日志写入
@Mapper
public interface StockDeductLogMapper extends BaseMapper<StockDeductLogDO> {}
