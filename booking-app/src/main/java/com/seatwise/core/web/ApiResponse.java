package com.seatwise.core.web;

import com.seatwise.core.BaseCode;
import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serializable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "기본 API 응답 템플릿")
public class ApiResponse<T> implements Serializable {

  @Schema(description = "응답 코드 (0: 성공 / 나머지 : 실패)")
  private Integer code;

  @Schema(description = "응답 메시지")
  private String message;

  @Schema(description = "응답 데이터")
  private T data;

  public static <T> ApiResponse<T> ok() {
    return ApiResponse.<T>builder()
        .code(BaseCode.SUCCESS.getCode())
        .message(BaseCode.SUCCESS.getMessage())
        .build();
  }

  public static <T> ApiResponse<T> ok(T data) {
    return ApiResponse.<T>builder()
        .code(BaseCode.SUCCESS.getCode())
        .message(BaseCode.SUCCESS.getMessage())
        .data(data)
        .build();
  }

  public static <T> ApiResponse<T> ok(T data, String message) {
    return ApiResponse.<T>builder()
        .code(BaseCode.SUCCESS.getCode())
        .message(message)
        .data(data)
        .build();
  }

  public static <T> ApiResponse<T> error(BaseCode baseCode) {
    return ApiResponse.<T>builder()
        .code(baseCode.getCode())
        .message(baseCode.getMessage())
        .build();
  }

  public static <T> ApiResponse<T> error(String message) {
    return ApiResponse.<T>builder()
        .code(BaseCode.SYSTEM_ERROR.getCode())
        .message(message)
        .build();
  }

  public static <T> ApiResponse<T> error(Integer code, String message) {
    return ApiResponse.<T>builder().code(code).message(message).build();
  }

  public static <T> ApiResponse<T> error(Integer code, String message, T data) {
    return ApiResponse.<T>builder().code(code).message(message).data(data).build();
  }
}
