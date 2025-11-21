package gighub.worketserver.global.exception;

import gighub.worketserver.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(RestApiException.class)
  protected ResponseEntity<ApiResponse<?>> handleRestApiException(RestApiException ex) {

    ErrorCode errorCode = ex.getErrorCode();

    String message = ex.getMessage();
    int httpStatus = errorCode.getHttpStatus().value();

    return ResponseEntity
      .status(httpStatus)
      .body(ApiResponse.error(message, errorCode.getCustomCode(), httpStatus));
  }

  /**
   * 404 잘못된 URL
   */
  @ExceptionHandler(NoResourceFoundException.class)
  protected ResponseEntity<ApiResponse<?>> handleNotFound(NoResourceFoundException ex) {

    ErrorCode errorCode = CommonErrorCode.NOT_FOUND;

    int httpStatus = errorCode.getHttpStatus().value();

    return ResponseEntity
      .status(httpStatus)
      .body(ApiResponse.error(errorCode.getMessage(), errorCode.getCustomCode(), httpStatus));
  }

  /**
   * 405 Method Not Allowed
   */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  protected ResponseEntity<ApiResponse<?>> handleMethodNotAllowed(HttpRequestMethodNotSupportedException ex) {

    ErrorCode errorCode = CommonErrorCode.METHOD_NOT_ALLOWED;

    int httpStatus = errorCode.getHttpStatus().value();

    return ResponseEntity
      .status(httpStatus)
      .body(ApiResponse.error(errorCode.getMessage(), errorCode.getCustomCode(), httpStatus));
  }

  /**
   * 예상 못한 모든 예외 (500)
   */
  @ExceptionHandler(Exception.class)
  protected ResponseEntity<ApiResponse<?>> handleException(Exception ex) {

    ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
    int httpStatus = errorCode.getHttpStatus().value();

    log.error("[500 InternalServerError] {}", ex.getMessage(), ex);

    return ResponseEntity
      .status(httpStatus)
      .body(ApiResponse.error(errorCode.getMessage(), errorCode.getCustomCode(), httpStatus));
  }

  @ExceptionHandler(CustomException.class)
  protected ResponseEntity<ApiResponse<?>> handleCustomException(CustomException ex) {

    ErrorCode errorCode = ex.getErrorCode();

    int httpStatus = errorCode.getHttpStatus().value();

    return ResponseEntity
      .status(httpStatus)
      .body(ApiResponse.error(errorCode.getMessage(), errorCode.getCustomCode(), httpStatus));
  }
}
