package gighub.worketserver.auth.dto;

import gighub.worketserver.user.constants.Provider;
import gighub.worketserver.user.constants.Role;
import gighub.worketserver.user.constants.Status;
import gighub.worketserver.user.User;
import lombok.Builder;

import java.util.Map;

@Builder
public record OAuth2UserInfo(
        String oauthId,
        String name
) {

    public static OAuth2UserInfo of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "kakao" -> ofKakao(attributes);

            // 필요시 다른 provider 추가 (google, naver 등)

            default -> throw new IllegalArgumentException("Unsupported registrationId: " + registrationId);
        };
    }

    @SuppressWarnings("unchecked")
    private static OAuth2UserInfo ofKakao(Map<String, Object> attributes) {
        // kakao 구조: id + kakao_account + kakao_account.profile
        String id = String.valueOf(attributes.get("id"));
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) account.get("profile");

        return OAuth2UserInfo.builder()
                .oauthId(id)
                .name((String) profile.get("nickname"))
                .build();
    }

    public User toEntity(Provider provider) {
        return User.builder()
                .oauthId(oauthId)
                .provider(provider)
                .name(name)
                .role(Role.FREELANCER) // 기본 역할
                .status(Status.ACTIVE) // 기본 상태
                .build();
    }
}
