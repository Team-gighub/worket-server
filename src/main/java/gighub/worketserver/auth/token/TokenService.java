package gighub.worketserver.auth.token;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class TokenService {

    private final TokenRepository tokenRepository;

    public void deleteRefreshToken(String memberKey) {
        tokenRepository.deleteById(memberKey);
    }

    @Transactional
    public void saveOrUpdate(String memberKey, String refreshToken, String accessToken) {
        tokenRepository.findById(memberKey).ifPresentOrElse(
                token -> {
                    token.updateAccessToken(accessToken);
                    token.updateRefreshToken(refreshToken);
                },
                () -> tokenRepository.save(new Token(memberKey, refreshToken, accessToken, null))
        );
    }

    public Token findByAccessTokenOrThrow(String accessToken) {
        return tokenRepository.findByAccessToken(accessToken)
                .orElseThrow(() -> new TokenException(TokenErrorCode.EXPIRED_TOKEN));
    }

    @Transactional
    public void updateToken(String accessToken, Token token) {
        token.updateAccessToken(accessToken);
        tokenRepository.save(token);
    }

    /** 카카오 access token 저장/갱신 */
    @Transactional
    public void saveKakaoAccessToken(String userId, String kakaoAccessToken) {
        tokenRepository.findById(userId).ifPresentOrElse(
                token -> token.updateKakaoAccessToken(kakaoAccessToken),
                () -> tokenRepository.save(new Token(userId, null, null, kakaoAccessToken))
        );
    }

    @Transactional(readOnly = true)
    public String findKakaoAccessToken(String userId) {
        return tokenRepository.findById(userId)
                .map(Token::getKakaoAccessToken)
                .orElse(null);
    }

    /** 카카오 access token 삭제 */
    @Transactional
    public void deleteKakaoAccessToken(String userId) {
        tokenRepository.findById(userId)
                .ifPresent(token -> token.updateKakaoAccessToken(null));
    }
}
