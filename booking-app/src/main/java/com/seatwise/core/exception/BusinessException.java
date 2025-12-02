package com.seatwise.core.exception;

import com.seatwise.core.ApiResponse;
import com.seatwise.core.BaseCode;
import lombok.Getter;

@Getter
public class BusinessException extends BaseException {

  private Integer code;
  private String message;

  public BusinessException() {
    super();
  }

  public BusinessException(String message) {
    super(message);
    this.message = message;
  }

  public BusinessException(String code, String message) {
    super(message);
    this.code = Integer.parseInt(code);
    this.message = message;
  }

  public BusinessException(Integer code, String message) {
    super(message);
    this.code = code;
    this.message = message;
  }

  public BusinessException(BaseCode baseCode) {
    super(baseCode.getMessage());
    this.code = baseCode.getCode();
    this.message = baseCode.getMessage();
  }

  public BusinessException(ApiResponse<?> apiResponse) {
    super(apiResponse.getMessage());
    this.code = apiResponse.getCode();
    this.message = apiResponse.getMessage();
  }

  public BusinessException(Throwable cause) {
    super(cause);
  }

  public BusinessException(String message, Throwable cause) {
    super(message, cause);
    this.message = message;
  }

  public BusinessException(Integer code, String message, Throwable cause) {
    super(message, cause);
    this.code = code;
    this.message = message;
  }
}
