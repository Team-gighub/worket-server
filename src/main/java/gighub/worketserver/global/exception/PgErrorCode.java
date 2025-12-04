package gighub.worketserver.global.exception;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

@Slf4j
@Getter
public enum PgErrorCode implements ErrorCode{

  // TODO: 관련 기능별 추가 및 정리

  // 400 Bad Request
  INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "COMMON_001", "입력 값이 올바르지 않습니다."),
  INVALID_JSON(HttpStatus.BAD_REQUEST, "COMMON_002", "JSON 형식이 잘못되었습니다."),

  // 401 Unauthorized
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_003", "인증이 필요합니다."),

  // 403 Forbidden
  FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_004", "접근 권한이 없습니다."),
  BALANCE_INSUFFICIENT(HttpStatus.FORBIDDEN, "BAL_3001", "잔액이 부족합니다."),
  VALIDATION_TOKEN_EXPIRED(HttpStatus.FORBIDDEN, "VAL_3001", "검증 토큰이 만료되었습니다."),
  ACCOUNT_ABNORMAL_STATUS(HttpStatus.FORBIDDEN, "ACCT_3001", "계좌가 비정상 상태입니다."),

  // 404 Not Found
  ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "ACCT_4001", "계좌를 조회할 수 없습니다."),
  ESCROW_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "ESC_4001", "에스크로 계좌를 조회할 수 없습니다."),
  PAYEE_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "ESC_4002", "수취인 계좌를 조회할 수 없습니다."),
  PLATFORM_ACCOUNT_NOT_FOUND(HttpStatus.NOT_FOUND, "ESC_4003", "플랫폼 계좌를 조회할 수 없습니다."),

  // 405 Method Not Allowed
  METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_005", "지원하지 않는 HTTP 메서드입니다."),

  // 409 Conflict
  ESCROW_ALREADY_RELEASED(HttpStatus.CONFLICT, "ESC_4091", "이미 지급확정된 에스크로입니다."),
  ESCROW_ALREADY_CANCELLED(HttpStatus.CONFLICT, "ESC_4092", "이미 취소된 에스크로입니다."),
  ESCROW_NOT_ACTIVE(HttpStatus.CONFLICT, "ESC_4093", "활성 상태가 아닌 에스크로입니다."),
  PAYEE_ACCOUNT_NOT_ACTIVE(HttpStatus.CONFLICT, "ESC_4094", "수취인 계좌가 활성 상태가 아닙니다."),
  PLATFORM_ACCOUNT_NOT_ACTIVE(HttpStatus.CONFLICT, "ESC_4095", "플랫폼 계좌가 활성 상태가 아닙니다."),

  // 422 Unprocessable Entity
  INVALID_ESCROW_AMOUNT(HttpStatus.UNPROCESSABLE_ENTITY, "ESC_4221", "에스크로 보유 금액이 유효하지 않습니다."),
  MERCHANT_ID_MISMATCH(HttpStatus.UNPROCESSABLE_ENTITY, "ESC_4222", "고객사 ID가 일치하지 않습니다."),
  AMOUNT_MISMATCH(HttpStatus.UNPROCESSABLE_ENTITY, "ESC_4223", "에스크로 금액 합계가 일치하지 않습니다."),

  // 500 Internal Server Error
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_006", "서버 내부 오류가 발생했습니다."),
  EXTERNAL_DEPOSIT_NOT_POSSIBLE(HttpStatus.INTERNAL_SERVER_ERROR, "EXTERNAL_001", "타행 이체가 불가능합니다."),
  EXTERNAL_DEPOSIT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "EXTERNAL_002", "타행 입금 요청이 실패했습니다."),

  // 501 Not Implemented
  NOT_IMPLEMENTED(HttpStatus.NOT_IMPLEMENTED, "COMMON_007", "아직 구현되지 않은 기능입니다.");

  private final HttpStatus httpStatus;
  private final String customCode;
  private final String message;


  PgErrorCode(HttpStatus httpStatus, String code, String message) {
    this.httpStatus = httpStatus;
    this.customCode = code;
    this.message = message;
  }

  public static PgErrorCode fromPgErrorCode(String code) {
    for (PgErrorCode error : PgErrorCode.values()) {
      if (error.customCode.equals(code)) {
        return error;
      }
    }
    log.error("매핑되지 않은 외부 PG 오류 코드 수신: {}", code);
    return INTERNAL_SERVER_ERROR; // 매핑되는 코드가 없을 경우 기본 에러를 반환합니다.
  }
}
