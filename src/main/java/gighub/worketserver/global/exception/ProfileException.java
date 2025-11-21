package gighub.worketserver.global.exception;

import lombok.Getter;
import org.springframework.security.core.AuthenticationException;

@Getter
public class ProfileException extends AuthenticationException {

  private final ProfileErrorCode errorCode;

  public ProfileException(ProfileErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }
}
