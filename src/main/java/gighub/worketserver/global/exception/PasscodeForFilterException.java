package gighub.worketserver.global.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class PasscodeForFilterException extends AuthenticationException {
  private final PasscodeForFilterErrorCode errorCode;

  public PasscodeForFilterException(PasscodeForFilterErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}

