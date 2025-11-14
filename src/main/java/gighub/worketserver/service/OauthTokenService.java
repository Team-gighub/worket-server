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

  /** OAuth Access Token 조회 */
  @Transactional(readOnly = true)
  public String findOauthAccessToken(Long userId, Provider provider) {
    return oauthTokenRepository.findByUserIdAndProvider(userId, provider)
      .map(OauthToken::getAccessToken)
      .orElse(null);
  }

  @Transactional
  public void deleteOauthAccessToken(Long userId, Provider provider) {
    oauthTokenRepository.findByUserIdAndProvider(userId, provider)
      .ifPresent(oauthTokenRepository::delete);
  }
}
