package gighub.worketserver.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PasscodeForFilterErrorCode implements ErrorCode {
  PASSCODE_EMPTY(HttpStatus.FORBIDDEN, "AUTH_3001", "패스코드가 등록되지 않았습니다."),
  PASSCODE_EMPTY_FREELANCER(HttpStatus.FORBIDDEN, "AUTH_3011", "프리랜서의 패스코드가 등록되지 않았습니다."),
  PASSCODE_EMPTY_CLIENT(HttpStatus.FORBIDDEN, "AUTH_3021", "클라이언트의 패스코드가 등록되지 않았습니다.");

  private final HttpStatus httpStatus;
  private final String customCode;
  private final String message;
}

