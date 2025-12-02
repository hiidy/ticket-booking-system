package com.seatwise.core.advice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seatwise.core.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@Slf4j
@RequiredArgsConstructor
@RestControllerAdvice
public class ApiResponseWrapperAdvice implements ResponseBodyAdvice<Object> {

  private final ObjectMapper objectMapper;

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

    if (body instanceof ApiResponse) {
      return body;
    }

    if (selectedConverterType.equals(StringHttpMessageConverter.class)) {
      try {
        return objectMapper.writeValueAsString(ApiResponse.ok(body));
      } catch (JsonProcessingException e) {
        throw new RuntimeException("String response wrapping failed", e);
      }
    }

    if (body == null) {
      return ApiResponse.ok("처리가 완료되었습니다.");
    }

    return ApiResponse.ok(body);
  }

  private boolean isSwaggerRequest(ServerHttpRequest request) {
    String path = request.getURI().getPath();
    return path != null
        && (path.contains("/swagger-ui")
            || path.contains("/swagger-resources")
            || path.contains("/v3/api-docs")
            || path.contains("/api-docs")
            || path.contains("/webjars/springfox-swagger-ui")
            || path.contains("/webjars/swagger-ui"));
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

    return declaringClassName.contains("OpenApi") || declaringClassName.contains("Swagger");
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
