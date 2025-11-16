package com.seatwise.core.web;

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
@Schema(description = "API 응답 표준 구조")
public class ApiResponse<T> implements Serializable {

  @Schema(description = "응답 코드 (0: 성공, 그외: 실패)")
  private Integer code;

  @Schema(description = "응답 메시지")
  private String message;

  @Schema(description = "응답 데이터")
  private T data;

  public static <T> ApiResponse<T> ok() {
    return ApiResponse.<T>builder().code(0).build();
  }

  public static <T> ApiResponse<T> ok(T data) {
    return ApiResponse.<T>builder().code(0).data(data).build();
  }

  public static <T> ApiResponse<T> ok(T data, String message) {
    return ApiResponse.<T>builder().code(0).message(message).data(data).build();
  }

  public static <T> ApiResponse<T> error(String message) {
    return ApiResponse.<T>builder().code(-100).message(message).build();
  }

  public static <T> ApiResponse<T> error(Integer code, String message) {
    return ApiResponse.<T>builder().code(code).message(message).build();
  }

  public static <T> ApiResponse<T> error(Integer code, String message, T data) {
    return ApiResponse.<T>builder().code(code).message(message).data(data).build();
  }
}
