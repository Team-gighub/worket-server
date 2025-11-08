package gighub.worketserver.auth.service;

import gighub.worketserver.auth.dto.OAuth2UserInfo;
import gighub.worketserver.auth.dto.PrincipalDetails;
import gighub.worketserver.user.User;
import gighub.worketserver.user.UserRepository;
import gighub.worketserver.user.constants.Provider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // 1. OAuth2 provider에서 유저 정보(attributes) 가져오기
        Map<String, Object> oAuth2UserAttributes = super.loadUser(userRequest).getAttributes();

        // 2. 어떤 provider인지 식별 (ex. "kakao", "google")
        String registrationId = userRequest.getClientRegistration().getRegistrationId();

        // 3. provider에서 유저를 구분하는 key (보통 "id")
        String userNameAttributeName = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        // 4. provider별 DTO 변환 (OAuth2UserInfo)
        OAuth2UserInfo oAuth2UserInfo = OAuth2UserInfo.of(registrationId, oAuth2UserAttributes);

        // 5️. DB 저장 or 기존 유저 조회
        User user = getOrSave(oAuth2UserInfo, registrationId);

        // 6. PrincipalDetails로 감싸서 반환
        return new PrincipalDetails(user, oAuth2UserAttributes, userNameAttributeName);
    }

    private User getOrSave(OAuth2UserInfo oAuth2UserInfo, String registrationId) {
        Provider provider = Provider.valueOf(registrationId.toUpperCase());

        return userRepository.findByOauthIdAndProvider(oAuth2UserInfo.oauthId(), provider)
                .orElseGet(() -> userRepository.save(oAuth2UserInfo.toEntity(provider)));
    }
}
