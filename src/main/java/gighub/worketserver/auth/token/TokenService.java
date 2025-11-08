package gighub.worketserver.auth.token;

import gighub.worketserver.user.constants.Provider;
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
    public void saveOrUpdate(String oauthId, String refreshToken, String accessToken) {
        tokenRepository.findById(oauthId).ifPresentOrElse(
                token -> {
                    token.updateAccessToken(accessToken);
                    token.updateRefreshToken(refreshToken);
                },
                () -> tokenRepository.save(new Token(oauthId, refreshToken, accessToken, null, null))
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

    /** access token 저장/갱신 */
    @Transactional
    public void saveOauthAccessToken(String userId, String oauthAccessToken, Provider provider) {
      tokenRepository.findById(userId).ifPresentOrElse(
        token -> token.updateOauthAccessToken(oauthAccessToken, provider),
        () -> tokenRepository.save(new Token(userId, null, null, oauthAccessToken, provider))
      );
    }

    @Transactional(readOnly = true)
    public String findOauthAccessToken(String userId) {
        return tokenRepository.findById(userId)
                .map(Token::getOauthAccessToken)
                .orElse(null);
    }

    /** oauth access token 삭제 */
    @Transactional
    public void deleteOauthAccessToken(String userId) {
      tokenRepository.deleteById(userId);
    }
}
