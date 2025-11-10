package gighub.worketserver.global.security.token;

import gighub.worketserver.global.security.token.constants.Provider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Token {

    @Id
    private String id;

    private String refreshToken; // JWT 리프레시 토큰
    private String accessToken;  // JWT 액세스 토큰
    private String oauthAccessToken;

    @Enumerated(EnumType.STRING)
    private Provider provider;

    public void updateAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void updateOauthAccessToken(String oauthAccessToken, Provider provider) {
        this.oauthAccessToken = oauthAccessToken;
        this.provider = provider;
    }
}
