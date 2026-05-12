package com.seckill.framework.web;

import com.seckill.framework.exception.AbstractException;
import com.seckill.framework.exception.ClientException;
import com.seckill.framework.result.Result;
import com.seckill.framework.result.Results;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(ClientException.class)
  public Result<Void> handleClientException(ClientException e) {
    log.warn("客户端异常: code={}, msg={}", e.getErrorCode(), e.getErrorMessage());
    return Results.failure(e);
  }

  @ExceptionHandler(AbstractException.class)
  public Result<Void> handleAbstractException(AbstractException e) {
    log.error("服务异常: code={}, msg={}", e.getErrorCode(), e.getErrorMessage(), e);
    return Results.failure(e);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public Result<Void> handleValidation(MethodArgumentNotValidException e) {
    String message =
        e.getBindingResult().getFieldErrors().stream()
            .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
            .reduce((a, b) -> a + "; " + b)
            .orElse("参数校验失败");
    return Results.failure("A000100", message);
  }

  @ExceptionHandler(Throwable.class)
  public Result<Void> handleThrowable(Throwable e) {
    log.error("未知异常", e);
    return Results.failure("B000999", "系统内部错误");
  }
}
