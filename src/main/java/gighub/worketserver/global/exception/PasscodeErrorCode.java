package gighub.worketserver.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PasscodeErrorCode implements ErrorCode {
  PASSCODE_EMPTY(HttpStatus.UNAUTHORIZED, "AUTH-2001", "패스코드가 비어 있습니다.");

  private final HttpStatus httpStatus;
  private final String code;
  private final String message;
}

