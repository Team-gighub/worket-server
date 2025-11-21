package gighub.worketserver.global.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class PasscodeException extends AuthenticationException {

  private final PasscodeErrorCode errorCode;

  public PasscodeException(PasscodeErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
