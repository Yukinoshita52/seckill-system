package com.seckill.engine.service;

import com.seckill.engine.dto.req.LoginReqDTO;
import com.seckill.engine.dto.req.RegisterReqDTO;
import com.seckill.engine.dto.resp.LoginRespDTO;

/** 认证服务 */
public interface AuthService {

  /**
   * 用户登录
   *
   * @param req 登录请求
   * @return token 和用户信息
   */
  LoginRespDTO login(LoginReqDTO req);

  /**
   * 用户注册
   *
   * @param req 注册请求
   */
  void register(RegisterReqDTO req);
}
