package com.seckill.common.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.seckill.common.dao.entity.UserDO;
import org.apache.ibatis.annotations.Mapper;

// todo: 待接入用户认证/管理模块
@Mapper
public interface UserMapper extends BaseMapper<UserDO> {}
