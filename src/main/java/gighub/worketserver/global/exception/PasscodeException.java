package gighub.worketserver.global.exception;

import lombok.Getter;

@Getter
public class PasscodeException extends CustomException {

  public PasscodeException(PasscodeErrorCode errorCode) {
    super(errorCode);
  }
}
