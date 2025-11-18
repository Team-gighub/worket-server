package gighub.worketserver.global.security.service;

import gighub.worketserver.domain.OauthToken;
import gighub.worketserver.domain.User;
import gighub.worketserver.domain.constants.Provider;
import gighub.worketserver.domain.constants.Role;
import gighub.worketserver.global.security.dto.OAuth2UserInfo;
import gighub.worketserver.global.security.dto.PrincipalDetails;
import gighub.worketserver.global.security.repository.CustomAuthorizationRequestRepository;
import gighub.worketserver.repository.OauthTokenRepository;
import gighub.worketserver.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;
  private final OauthTokenRepository oauthTokenRepository;
  private final CustomAuthorizationRequestRepository customAuthorizationRequestRepository;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    Map<String, Object> oAuth2UserAttributes = super.loadUser(userRequest).getAttributes();

    // OAuth 토큰 정보 추출
    String accessToken = userRequest.getAccessToken().getTokenValue();

    Object refreshTokenObj = userRequest.getAdditionalParameters().get("refresh_token");
    String refreshToken = refreshTokenObj != null ? refreshTokenObj.toString() : null;

    Object refreshTokenExpiresInObj = userRequest.getAdditionalParameters().get("refresh_token_expires_in");
    Long refreshTokenExpiresIn = refreshTokenExpiresInObj != null
      ? Long.parseLong(refreshTokenExpiresInObj.toString())
      : null;

    Instant refreshTokenExpiresAt = refreshTokenExpiresIn != null
      ? Instant.now().plusSeconds(refreshTokenExpiresIn)
      : null;

    HttpServletRequest request =
      ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

    String state = customAuthorizationRequestRepository.getSavedState(request);

    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    String userNameAttributeName = userRequest.getClientRegistration()
      .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

    OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(registrationId, oAuth2UserAttributes);

    // 사용자 조회 또는 생성
    User user = getOrSave(oAuth2UserInfo, state, registrationId);

    // OAuth 토큰 저장 또는 업데이트
    saveOrUpdateOauthToken(
      user.getId(),
      registrationId,
      accessToken,
      refreshToken,
      refreshTokenExpiresAt
    );

    return new PrincipalDetails(user, oAuth2UserAttributes, userNameAttributeName);
  }

  private User getOrSave(OAuth2UserInfo oAuth2UserInfo, String state, String registrationId) {
    return userRepository.findByOauthId(oAuth2UserInfo.oauthId())
      .orElseGet(() -> {
        Role role = ("CLIENT".equalsIgnoreCase(state))
          ? Role.CLIENT
          : Role.FREELANCER;

        Provider provider = Provider.valueOf(registrationId.toUpperCase());
        User newUser = User.create(
          provider,
          oAuth2UserInfo.oauthId(),
          oAuth2UserInfo.name(),
          role
        );

        return userRepository.save(newUser);
      });
  }

  private void saveOrUpdateOauthToken(Long userId, String registrationId,
                                      String accessToken, String refreshToken,
                                      Instant refreshTokenExpiresAt) {
    Provider provider = Provider.valueOf(registrationId.toUpperCase());

    // 타입 변환 로직
    LocalDateTime refreshExpires = refreshTokenExpiresAt != null
      ? LocalDateTime.ofInstant(refreshTokenExpiresAt, ZoneId.systemDefault())
      : null;

    OauthToken oauthToken = oauthTokenRepository.findByUserIdAndProvider(userId, provider)
      .orElseGet(() -> OauthToken.create(userId, provider));

    oauthToken.updateTokens(accessToken, refreshToken, refreshExpires);

    oauthTokenRepository.save(oauthToken);
  }
}
