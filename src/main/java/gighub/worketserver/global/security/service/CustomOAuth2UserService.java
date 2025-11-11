package gighub.worketserver.global.security.service;

import gighub.worketserver.domain.User;
import gighub.worketserver.domain.constants.Role;
import gighub.worketserver.global.security.dto.OAuth2UserInfo;
import gighub.worketserver.global.security.dto.PrincipalDetails;
import gighub.worketserver.repository.CustomAuthorizationRequestRepository;
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

import java.util.Map;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final UserRepository userRepository;
  private final CustomAuthorizationRequestRepository customAuthorizationRequestRepository; // ✅ 주입받기

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    Map<String, Object> oAuth2UserAttributes = super.loadUser(userRequest).getAttributes();

    HttpServletRequest request =
      ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

    String state = customAuthorizationRequestRepository.getSavedState(request);

    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    String userNameAttributeName = userRequest.getClientRegistration()
      .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

    OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(registrationId, oAuth2UserAttributes);

    User user = getOrSave(oAuth2UserInfo, state);

    return new PrincipalDetails(user, oAuth2UserAttributes, userNameAttributeName);
  }

  private User getOrSave(OAuth2UserInfo oAuth2UserInfo, String state) {
    return userRepository.findByOauthId(oAuth2UserInfo.oauthId())
      .orElseGet(() -> {

        Role role = ("CLIENT".equalsIgnoreCase(state))
          ? Role.CLIENT
          : Role.FREELANCER;

        User newUser = oAuth2UserInfo.toEntity(role);
        newUser.setRole(role);

        User savedUser = userRepository.save(newUser);
        return savedUser;
      });
  }
}
