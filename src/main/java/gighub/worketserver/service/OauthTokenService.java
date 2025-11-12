package gighub.worketserver.service;

import gighub.worketserver.domain.OauthToken;
import gighub.worketserver.domain.User;
import gighub.worketserver.domain.constants.Provider;
import gighub.worketserver.repository.OauthTokenRepository;
import gighub.worketserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OauthTokenService {

  private final OauthTokenRepository oauthTokenRepository;
  private final UserRepository userRepository;

  /** OAuth Access Token 저장 또는 갱신 */
  @Transactional
  public void saveOauthAccessToken(String oauthId, String oauthAccessToken, Provider provider) {
    // oauthId로 사용자 조회
    User user = userRepository.findById(Long.valueOf(oauthId))
      .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + oauthId));

    // 기존 토큰 조회 또는 새로 생성
    OauthToken oauthToken = oauthTokenRepository.findByUserIdAndProvider(user.getId(), provider)
      .orElseGet(() -> {
        OauthToken newToken = new OauthToken();
        newToken.setUserId(user.getId());
        newToken.setProvider(provider);
        return newToken;
      });

    // OAuth Access Token 업데이트
    oauthToken.updateAccessToken(oauthAccessToken);
    oauthTokenRepository.save(oauthToken);

    log.info("OAuth Access Token 저장 완료 - UserId: {}, Provider: {}", user.getId(), provider);
  }

  /** OAuth Access Token 조회 */
  @Transactional(readOnly = true)
  public String findOauthAccessToken(Long userId, Provider provider) {
    return oauthTokenRepository.findByUserIdAndProvider(userId, provider)
      .map(OauthToken::getAccessToken)
      .orElse(null);
  }

  /** OAuth Access Token 삭제 */
  @Transactional
  public void deleteOauthAccessToken(Long userId, Provider provider) {
    oauthTokenRepository.findByUserIdAndProvider(userId, provider)
      .ifPresent(token -> {
        // Refresh Token이 없으면 완전히 삭제
        if (token.getRefreshToken() == null) {
          oauthTokenRepository.delete(token);
        } else {
          // Access Token만 null로 설정
          token.setAccessToken(null);
          token.setRefreshToken(null);
          oauthTokenRepository.save(token);
        }
      });
  }
}
