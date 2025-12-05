package gighub.worketserver.global.exception;

import lombok.Getter;

@Getter
public class PgException extends CustomException {
  private final PgErrorCode errorCode;

  public PgException(PgErrorCode errorCode) {
    super(errorCode);
    this.errorCode = errorCode;
  }
}
