package com.seatwise.core.advice;

import com.seatwise.core.BaseCode;
import com.seatwise.core.exception.BaseCodeException;
import com.seatwise.core.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Slf4j
@RestControllerAdvice
public class ApiResponseWrapperAdvice implements ResponseBodyAdvice<Object> {

  @Override
  public boolean supports(
      MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {

    return !returnType.getParameterType().equals(ApiResponse.class)
        && !isSwaggerEndpoint(returnType)
        && !isActuatorEndpoint(returnType);
  }

  @Override
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {

    if (isSwaggerRequest(request) || isActuatorRequest(request)) {
      return body;
    }

    // 이미 ApiResponse 형태이면 그대로 반환
    if (body instanceof ApiResponse) {
      return body;
    }

    // 예외 처리
    if (body instanceof BaseCodeException exception) {
      log.warn("Handled {} : {}", exception.getClass().getSimpleName(), exception.getBaseCode().name(), exception);
      return ApiResponse.error(exception.getBaseCode());
    }

    // 일반 예외 처리
    if (body instanceof Exception exception) {
      log.error("Unhandled exception: ", exception);
      return ApiResponse.error(BaseCode.SYSTEM_ERROR);
    }

    // 정상 응답 처리
    if (body == null || returnType.getParameterType().equals(Void.class)) {
      return ApiResponse.ok("처리가 완료되었습니다");
    }

    return ApiResponse.ok(body);
  }

  @ExceptionHandler(Exception.class)
  public ApiResponse<Void> handleException(Exception e) {
    log.error("Unhandled exception: ", e);
    return ApiResponse.error(BaseCode.SYSTEM_ERROR);
  }

  @ExceptionHandler(BaseCodeException.class)
  public ApiResponse<Void> handleBaseCodeException(BaseCodeException e) {
    log.warn("Handled {} : {}", e.getClass().getSimpleName(), e.getBaseCode().name(), e);
    return ApiResponse.error(e.getBaseCode());
  }

  private boolean isSwaggerRequest(ServerHttpRequest request) {
    String path = request.getURI().getPath();
    return path != null && (
        path.contains("/swagger-ui")
        || path.contains("/swagger-resources")
        || path.contains("/v3/api-docs")
        || path.contains("/api-docs")
        || path.contains("/webjars/springfox-swagger-ui")
        || path.contains("/webjars/swagger-ui")
    );
  }

  private boolean isActuatorRequest(ServerHttpRequest request) {
    String path = request.getURI().getPath();
    return path != null && path.contains("/actuator");
  }

  private boolean isSwaggerEndpoint(MethodParameter returnType) {
    String declaringClassName = returnType.getDeclaringClass().getSimpleName();

    if (returnType.getMethod() != null) {
      String methodName = returnType.getMethod().getName();
      return declaringClassName.contains("OpenApi")
          || declaringClassName.contains("Swagger")
          || methodName.contains("openapi");
    }

    return declaringClassName.contains("OpenApi")
        || declaringClassName.contains("Swagger");
  }

  private boolean isActuatorEndpoint(MethodParameter returnType) {
    String declaringClassName = returnType.getDeclaringClass().getSimpleName();

    return declaringClassName.contains("Actuator")
        || declaringClassName.contains("Health")
        || declaringClassName.contains("Info")
        || declaringClassName.contains("Metrics")
        || declaringClassName.contains("Environment");
  }
}
