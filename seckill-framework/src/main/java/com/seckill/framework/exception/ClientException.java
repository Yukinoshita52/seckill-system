package com.seckill.framework.exception;

import com.seckill.framework.errorcode.BaseErrorCode;

public class ClientException extends AbstractException {
  public ClientException(String errorCode, String errorMessage) {
    super(errorCode, errorMessage);
  }

  public ClientException(BaseErrorCode errorCode) {
    super(errorCode);
  }
}
