package com.seckill.framework.exception;

import com.seckill.framework.errorcode.BaseErrorCode;

public class RemoteException extends AbstractException {
  public RemoteException(String errorCode, String errorMessage) {
    super(errorCode, errorMessage);
  }

  public RemoteException(BaseErrorCode errorCode) {
    super(errorCode);
  }
}
