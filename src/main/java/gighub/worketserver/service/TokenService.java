package gighub.worketserver.service;

import gighub.worketserver.domain.Token;
import gighub.worketserver.domain.constants.Provider;
import gighub.worketserver.global.exception.TokenErrorCode;
import gighub.worketserver.global.exception.TokenException;
import gighub.worketserver.repository.TokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class TokenService {

  private final TokenRepository tokenRepository;

  /** Refresh Token 저장 또는 갱신 */
  @Transactional
  public void saveOrUpdateRefreshToken(String oauthId, String refreshToken) {
    tokenRepository.findById(oauthId).ifPresentOrElse(
      token -> token.updateRefreshToken(refreshToken),
      () -> tokenRepository.save(new Token(oauthId, refreshToken, null, null))
    );
  }

  /** Refresh Token 조회 */
  @Transactional(readOnly = true)
  public String findRefreshTokenOrThrow(String oauthId) {
    return tokenRepository.findById(oauthId)
      .map(Token::getRefreshToken)
      .orElseThrow(() -> new TokenException(TokenErrorCode.EXPIRED_TOKEN));
  }

  /** Refresh Token 삭제 (로그아웃 시) */
  @Transactional
  public void deleteRefreshToken(String oauthId) {
    tokenRepository.deleteById(oauthId);
  }

  /** 카카오 OAuth Access Token 저장 */
  @Transactional
  public void saveOauthAccessToken(String userId, String oauthAccessToken, Provider provider) {
    tokenRepository.findById(userId).ifPresentOrElse(
      token -> token.updateOauthAccessToken(oauthAccessToken, provider),
      () -> tokenRepository.save(new Token(userId, null, oauthAccessToken, provider))
    );
  }

  /** 카카오 OAuth Access Token 조회 */
  @Transactional(readOnly = true)
  public String findOauthAccessToken(String userId) {
    return tokenRepository.findById(userId)
      .map(Token::getOauthAccessToken)
      .orElse(null);
  }

  /** 카카오 OAuth Access Token 삭제 */
  @Transactional
  public void deleteOauthAccessToken(String userId) {
    tokenRepository.deleteById(userId);
  }
}
