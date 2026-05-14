package com.seckill.framework.result;

import com.seckill.framework.exception.AbstractException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public final class Results {

  public static <T> Result<T> success(T data) {
    return new Result<>("0", "success", data, getRequestId());
  }

  public static Result<Void> success() {
    return new Result<>("0", "success", null, getRequestId());
  }

  public static <T> Result<T> failure(String code, String message) {
    return new Result<>(code, message, null, getRequestId());
  }

  public static <T> Result<T> failure(AbstractException e) {
    return new Result<>(e.getErrorCode(), e.getErrorMessage(), null, getRequestId());
  }

  private static String getRequestId() {
    try {
      ServletRequestAttributes attrs =
          (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
      if (attrs != null) {
        HttpServletRequest request = attrs.getRequest();
        String traceId = request.getHeader("X-Trace-Id");
        return traceId != null ? traceId : "";
      }
    } catch (Exception ignored) {
    }
    return "";
  }
}
