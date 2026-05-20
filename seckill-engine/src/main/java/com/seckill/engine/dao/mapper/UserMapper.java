package com.seckill.engine.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.engine.dao.entity.UserDO;
import org.apache.ibatis.annotations.Mapper;

// todo: 待接入用户认证/管理模块
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {}