package com.seatwise.booking.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
  private boolean success;
  private String errorCode;
  private String message;
  private List<String> details;
  private long timestamp;

  public static ErrorResponse of(String errorCode, String message) {
    return ErrorResponse.builder()
        .success(false)
        .errorCode(errorCode)
        .message(message)
        .timestamp(System.currentTimeMillis())
        .build();
  }

  public static ErrorResponse of(String errorCode, String message, List<String> details) {
    return ErrorResponse.builder()
        .success(false)
        .errorCode(errorCode)
        .message(message)
        .details(details)
        .timestamp(System.currentTimeMillis())
        .build();
  }
}
