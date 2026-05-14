package com.seckill.engine.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.seckill.common.dao.entity.UserDO;
import com.seckill.common.dao.mapper.UserMapper;
import com.seckill.engine.dto.req.LoginReqDTO;
import com.seckill.engine.dto.req.RegisterReqDTO;
import com.seckill.engine.dto.resp.LoginRespDTO;
import com.seckill.engine.service.AuthService;
import com.seckill.framework.exception.ClientException;
import com.seckill.framework.toolkit.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

  private final UserMapper userMapper;
  private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

  @Value("${jwt.secret}")
  private String jwtSecret;

  @Value("${jwt.expiration}")
  private long jwtExpiration;

  @Override
  public LoginRespDTO login(LoginReqDTO req) {
    UserDO user = userMapper.selectOne(
        new LambdaQueryWrapper<UserDO>().eq(UserDO::getUsername, req.getUsername()));
    if (user == null || !ENCODER.matches(req.getPassword(), user.getPassword())) {
      throw new ClientException("A000500", "用户名或密码错误");
    }

    String token = JwtUtil.generateToken(user.getId(), user.getUsername(), jwtSecret, jwtExpiration);

    return LoginRespDTO.builder()
        .token(token)
        .userId(user.getId())
        .username(user.getUsername())
        .build();
  }

  @Override
  public void register(RegisterReqDTO req) {
    Long count = userMapper.selectCount(
        new LambdaQueryWrapper<UserDO>().eq(UserDO::getUsername, req.getUsername()));
    if (count > 0) {
      throw new ClientException("A000500", "用户名已存在");
    }

    UserDO user = UserDO.builder()
        .username(req.getUsername())
        .password(ENCODER.encode(req.getPassword()))
        .nickname(req.getNickname())
        .build();
    userMapper.insert(user);
  }
}
