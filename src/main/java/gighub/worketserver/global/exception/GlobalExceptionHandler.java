package gighub.worketserver.global.exception;

import gighub.worketserver.global.response.ApiResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * ErrorCode 기반 Custom Exception 처리
   */
  @ExceptionHandler(RestApiException.class)
  protected ApiResponse<?> handleRestApiException(RestApiException ex) {
    ErrorCode errorCode = ex.getErrorCode();
    return ApiResponse.error(errorCode.getMessage());
  }

  /**
   * 예상 못한 모든 예외 처리
   */
  @ExceptionHandler(Exception.class)
  protected ApiResponse<?> handleException(Exception ex) {
    return ApiResponse.error(ex.getMessage());
  }
}
