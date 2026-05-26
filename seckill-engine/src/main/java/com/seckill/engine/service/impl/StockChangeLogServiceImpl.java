package com.seckill.engine.service.impl;

import com.seckill.engine.dao.entity.StockChangeLogDO;
import com.seckill.engine.dao.mapper.StockChangeLogMapper;
import com.seckill.engine.service.StockChangeLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class StockChangeLogServiceImpl implements StockChangeLogService {

  private final StockChangeLogMapper stockChangeLogMapper;

  @Override
  public void log(StockChangeLogDO logEntry) {
    stockChangeLogMapper.insert(logEntry);
  }
}