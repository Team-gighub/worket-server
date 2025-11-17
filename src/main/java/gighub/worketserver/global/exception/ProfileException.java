package gighub.worketserver.global.exception;

import org.springframework.security.core.AuthenticationException;

public class ProfileException extends AuthenticationException {

  private final ProfileErrorCode errorCode;

  public ProfileException(ProfileErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  public ProfileErrorCode getErrorCode() {
    return errorCode;
  }
}
