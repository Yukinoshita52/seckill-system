package com.seckill.framework.exception;

import com.seckill.framework.errorcode.BaseErrorCode;

public class ServiceException extends AbstractException {
  public ServiceException(String errorCode, String errorMessage) {
    super(errorCode, errorMessage);
  }

  public ServiceException(BaseErrorCode errorCode) {
    super(errorCode);
  }
}
