package com.seckill.engine.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.engine.dao.entity.UserDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserDO> {}
