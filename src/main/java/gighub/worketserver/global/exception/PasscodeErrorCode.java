package gighub.worketserver.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PasscodeErrorCode implements ErrorCode {
    PASSCODE_EMPTY(HttpStatus.FORBIDDEN, "AUTH_2001", "패스코드가 등록되지 않았습니다."),
    INVALID_PASSCODE(HttpStatus.UNAUTHORIZED, "AUTH_2002", "잘못된 패스코드입니다.");

    private final HttpStatus httpStatus;
    private final String customCode;
    private final String message;
}

