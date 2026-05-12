package com.seckill.framework.result;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {
  private String code;
  private String message;
  private T data;
  private String requestId;
}
