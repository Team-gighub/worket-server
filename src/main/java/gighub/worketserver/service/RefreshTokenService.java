package gighub.worketserver.service;

import gighub.worketserver.domain.UserRefreshToken;
import gighub.worketserver.global.exception.TokenErrorCode;
import gighub.worketserver.global.exception.TokenException;
import gighub.worketserver.repository.UserRefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class RefreshTokenService {

  private final UserRefreshTokenRepository userRefreshTokenRepository;

  /** JWT Refresh Token 저장 또는 갱신 */
  @Transactional
  public void saveRefreshToken(Long userId, String refreshToken, LocalDateTime expiresAt) {
    // 기존 유효한 토큰이 있으면 revoke 처리
    userRefreshTokenRepository.findByUserIdAndIsRevokedFalse(userId)
      .ifPresent(token -> {
        token.revoke();
        userRefreshTokenRepository.save(token);
        log.info("기존 Refresh Token revoke 처리 - UserId: {}", userId);
      });

    // 새 리프레시 토큰 저장
    UserRefreshToken newToken = new UserRefreshToken();
    newToken.setUserId(userId);
    newToken.setRefreshToken(refreshToken);
    newToken.setIssuedAt(LocalDateTime.now());
    newToken.setExpiresAt(expiresAt);
    newToken.setIsRevoked(false);

    userRefreshTokenRepository.save(newToken);
    log.info("새 Refresh Token 저장 완료 - UserId: {}", userId);
  }

  /** JWT Refresh Token 조회 */
  @Transactional(readOnly = true)
  public String findRefreshTokenOrThrow(Long userId) {
    return userRefreshTokenRepository.findByUserIdAndIsRevokedFalse(userId)
      .map(UserRefreshToken::getRefreshToken)
      .orElseThrow(() -> new TokenException(TokenErrorCode.EXPIRED_TOKEN));
  }

  /** JWT Refresh Token 검증 */
  public boolean isValidRefreshToken(String refreshToken) {
    return userRefreshTokenRepository.findByRefreshTokenAndIsRevokedFalse(refreshToken)
      .map(UserRefreshToken::isValid)
      .orElse(false);
  }

  /** 사용자의 모든 Refresh Token revoke (로그아웃 시) */
  @Transactional
  public void revokeAllRefreshTokens(Long userId) {
    userRefreshTokenRepository.findByUserIdAndIsRevokedFalse(userId)
      .ifPresent(token -> {
        token.revoke();
        userRefreshTokenRepository.save(token);
        log.info("사용자의 모든 Refresh Token revoke 완료 - UserId: {}", userId);
      });
  }
}
