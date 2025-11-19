package gighub.worketserver.global.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
  HttpStatus getHttpStatus();

  String getCustomCode();

  String getMessage();   // CustomException이 기본 메시지를 쓰기 위해 필요
}
