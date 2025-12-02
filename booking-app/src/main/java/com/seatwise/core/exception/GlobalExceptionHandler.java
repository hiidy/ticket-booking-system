package com.seatwise.core.exception;

import com.seatwise.core.ApiResponse;
import com.seatwise.core.BaseCode;
import com.seatwise.core.dto.ArgumentError;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(BusinessException.class)
  public ApiResponse<Void> handleBusinessException(
      HttpServletRequest request, BusinessException e) {
    log.error(
        "비즈니스 예외 발생 method: {}, url: {}, query: {},}",
        request.getMethod(),
        getRequestUrl(request),
        getRequestQuery(request));
    return ApiResponse.error(e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ApiResponse<List<ArgumentError>> handleValidationException(
      HttpServletRequest request, MethodArgumentNotValidException e) {
    log.error(
        "유효성 검증 예외 발생 method: {}, url: {}, query: {}",
        request.getMethod(),
        getRequestUrl(request),
        getRequestQuery(request),
        e);

    BindingResult bindingResult = e.getBindingResult();
    List<ArgumentError> argumentErrorList =
        bindingResult.getFieldErrors().stream()
            .map(
                fieldError -> {
                  ArgumentError argumentError = new ArgumentError();
                  argumentError.setArgumentName(fieldError.getField());
                  argumentError.setMessage(fieldError.getDefaultMessage());
                  return argumentError;
                })
            .collect(Collectors.toList());

    return ApiResponse.error(
        BaseCode.ARGUMENT_EMPTY.getCode(), "입력값이 유효하지 않습니다.", argumentErrorList);
  }

  @ExceptionHandler(Exception.class)
  public ApiResponse<Void> handleException(HttpServletRequest request, Exception e) {
    log.error(
        "처리되지 않은 예외 발생 method: {}, url: {}, query: {}",
        request.getMethod(),
        getRequestUrl(request),
        getRequestQuery(request),
        e);
    return ApiResponse.error(BaseCode.SYSTEM_ERROR);
  }

  @ExceptionHandler(Throwable.class)
  public ApiResponse<Void> handleThrowable(HttpServletRequest request, Throwable throwable) {
    log.error(
        "치명적인 오류 발생 method: {}, url: {}, query: {}",
        request.getMethod(),
        getRequestUrl(request),
        getRequestQuery(request),
        throwable);
    return ApiResponse.error(BaseCode.SYSTEM_ERROR);
  }

  private String getRequestUrl(HttpServletRequest request) {
    return request.getRequestURL().toString();
  }

  private String getRequestQuery(HttpServletRequest request) {
    return request.getQueryString();
  }
}
