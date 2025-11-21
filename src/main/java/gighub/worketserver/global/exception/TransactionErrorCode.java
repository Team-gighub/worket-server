package gighub.worketserver.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum TransactionErrorCode implements ErrorCode {

  CLIENT_LINKAGE_REQUIRED(HttpStatus.UNAUTHORIZED, "ACCESS_1001", "거래와 일치하는 클라이언트가 거래에 등록되어있지 않습니다."),
  TRANSACTION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "ACCESS_3001", "거래에 접근권한이 없습니다.");


  private final HttpStatus httpStatus;
  private final String customCode;
  private final String message;

  TransactionErrorCode(HttpStatus httpStatus, String code, String message) {
    this.httpStatus = httpStatus;
    this.customCode = code;
    this.message = message;
  }
}
