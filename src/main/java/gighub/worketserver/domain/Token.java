package gighub.worketserver.domain;

import gighub.worketserver.domain.constants.Provider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Token {

    @Id
    private String id;

    private String refreshToken; // JWT 리프레시 토큰
    private String oauthAccessToken;

    @Enumerated(EnumType.STRING)
    private Provider provider;


    public void updateOauthAccessToken(String oauthAccessToken, Provider provider) {
        this.oauthAccessToken = oauthAccessToken;
        this.provider = provider;
    }

    @Transactional
    public void updateRefreshToken(String refreshToken) {
      this.refreshToken = refreshToken;
    }
}
