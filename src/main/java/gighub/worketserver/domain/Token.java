package gighub.worketserver.domain;

import gighub.worketserver.domain.constants.Provider;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

@Getter
@Setter
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

    public void updateRefreshToken(String refreshToken) {
      this.refreshToken = refreshToken;
    }
}
