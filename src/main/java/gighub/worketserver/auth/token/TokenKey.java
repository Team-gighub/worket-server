package gighub.worketserver.auth.token;

public final class TokenKey {

    private TokenKey() {} // 인스턴스화 방지

    public static final String AUTHORIZATION = "Authorization"; // 헤더 이름
    public static final String TOKEN_PREFIX = "Bearer ";         // 접두사
}
