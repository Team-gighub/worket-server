package gighub.worketserver.global.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ProfileErrorCode implements ErrorCode {

  FREELANCER_PROFILE_NOT_FOUND(HttpStatus.FORBIDDEN, "AUTH_3001", "프리랜서 프로필이 존재하지 않습니다.");

  private final HttpStatus httpStatus;
  private final String customCode;
  private final String message;

  ProfileErrorCode(HttpStatus httpStatus, String code, String message) {
    this.httpStatus = httpStatus;
    this.customCode = code;
    this.message = message;
  }
}
