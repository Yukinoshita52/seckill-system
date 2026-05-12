package com.seckill.framework.exception;

import com.seckill.framework.errorcode.BaseErrorCode;
import lombok.Getter;

@Getter
public abstract class AbstractException extends RuntimeException {
  private final String errorCode;
  private final String errorMessage;

  protected AbstractException(String errorCode, String errorMessage) {
    super(errorMessage);
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
  }

  protected AbstractException(BaseErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode.getCode();
    this.errorMessage = errorCode.getMessage();
  }
}
