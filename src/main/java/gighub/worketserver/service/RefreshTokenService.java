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

  /**
   * JWT Refresh Token 저장 또는 갱신
   */
  @Transactional
  public void saveRefreshToken(Long userId, String refreshToken, LocalDateTime expiresAt) {
    // 기존 유효한 토큰이 있으면 revoke 처리
    userRefreshTokenRepository.findByUserIdAndIsRevokedFalse(userId)
      .ifPresent(token -> {
        token.revoke();
        userRefreshTokenRepository.save(token);
      });

    // 새 리프레시 토큰 저장
    UserRefreshToken newToken = UserRefreshToken.create(userId, refreshToken, expiresAt);

    userRefreshTokenRepository.save(newToken);
  }

  /**
   * JWT Refresh Token 조회
   */
  @Transactional(readOnly = true)
  public String findRefreshTokenOrThrow(Long userId) {
    return userRefreshTokenRepository.findByUserIdAndIsRevokedFalse(userId)
      .map(UserRefreshToken::getRefreshToken)
      .orElseThrow(() -> new TokenException(TokenErrorCode.EXPIRED_TOKEN));
  }

  /**
   * JWT Refresh Token 검증
   */
  public boolean isValidRefreshToken(String refreshToken) {
    return userRefreshTokenRepository.findByRefreshTokenAndIsRevokedFalse(refreshToken)
      .map(UserRefreshToken::isValid)
      .orElse(false);
  }

  /**
   * 사용자의 모든 Refresh Token revoke (로그아웃 시)
   */
  @Transactional
  public void revokeAllRefreshTokens(Long userId) {
    userRefreshTokenRepository.findByUserIdAndIsRevokedFalse(userId)
      .ifPresent(token -> {
        token.revoke();
        userRefreshTokenRepository.save(token);
      });
  }

  @Transactional(readOnly = true)
  public boolean isExpiringSoon(String refreshToken, int thresholdHours) {
    return userRefreshTokenRepository.findByRefreshToken(refreshToken)
      .map(token -> token.getExpiresAt().isBefore(LocalDateTime.now().plusHours(thresholdHours)))
      .orElse(false);
  }

  @Transactional
  public void rotateRefreshToken(String oldRefreshToken, String newRefreshToken) {
    // 1. 기존 refresh token 찾기
    UserRefreshToken existing = userRefreshTokenRepository.findByRefreshToken(oldRefreshToken)
      .orElseThrow(() -> new TokenException(TokenErrorCode.EXPIRED_TOKEN));

    // 2. 기존 토큰 revoke 처리
    existing.revoke();
    userRefreshTokenRepository.save(existing);

    // 3. 새 토큰 엔티티 생성 및 저장
    Long userId = existing.getUserId();
    LocalDateTime newExpiresAt = LocalDateTime.now().plusDays(7);

    UserRefreshToken newToken = UserRefreshToken.create(userId, newRefreshToken, newExpiresAt);

    userRefreshTokenRepository.save(newToken);
  }
}
