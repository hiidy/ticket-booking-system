package com.seatwise.booking.advice;

import com.seatwise.booking.dto.response.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
public class ApiResponseWrapperAdvice implements ResponseBodyAdvice<Object> {

  @Override
  public boolean supports(
      MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {

    return !returnType.getParameterType().equals(ApiResponse.class)
        && !isSwaggerEndpoint(returnType);
  }

  @Override
  public Object beforeBodyWrite(
      Object body,
      MethodParameter returnType,
      MediaType selectedContentType,
      Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      ServerHttpResponse response) {

    if (isSwaggerRequest(request)) {
      return body;
    }

    if (body instanceof ApiResponse) {
      return body;
    }

    if (body == null || returnType.getParameterType().equals(Void.class)) {
      return ApiResponse.ok("처리가 완료되었습니다");
    }

    return ApiResponse.ok(body);
  }

  private boolean isSwaggerRequest(ServerHttpRequest request) {
    String path = request.getURI().getPath();
    return path != null && (
        path.contains("/swagger")
        || path.contains("/v3/api-docs")
        || path.contains("/api-docs")
    );
  }

  private boolean isSwaggerEndpoint(MethodParameter returnType) {
    String declaringClassName = returnType.getDeclaringClass().getSimpleName();
    String methodName = returnType.getMethod().getName();

    return declaringClassName != null && (
        declaringClassName.contains("OpenApi")
        || declaringClassName.contains("Swagger")
        || (methodName != null && methodName.contains("openapi"))
    );
  }
}
