package gighub.worketserver.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PasscodeErrorCode implements ErrorCode {

  INVALID_PASSCODE(HttpStatus.UNAUTHORIZED, "AUTH_3002", "잘못된 패스코드입니다.");

  private final HttpStatus httpStatus;
  private final String customCode;
  private final String message;

}
