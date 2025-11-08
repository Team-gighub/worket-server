package gighub.worketserver.auth.token;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
    private String kakaoAccessToken; // 카카오에서 받은 액세스 토큰

    public void updateAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void updateRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public void updateKakaoAccessToken(String kakaoAccessToken) {
        this.kakaoAccessToken = kakaoAccessToken;
    }
}
