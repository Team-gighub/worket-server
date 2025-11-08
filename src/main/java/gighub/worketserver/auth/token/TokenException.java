package gighub.worketserver.auth.token;

import lombok.Getter;

@Getter
public class TokenException extends RuntimeException {

    private final TokenErrorCode errorCode;

    public TokenException(TokenErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}

