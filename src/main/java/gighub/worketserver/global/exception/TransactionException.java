package gighub.worketserver.global.exception;

import lombok.Getter;

@Getter
public class TransactionException extends CustomException {
  private final TransactionErrorCode errorCode;

  public TransactionException(TransactionErrorCode errorCode) {
    super(errorCode);
    this.errorCode = errorCode;
  }
}
