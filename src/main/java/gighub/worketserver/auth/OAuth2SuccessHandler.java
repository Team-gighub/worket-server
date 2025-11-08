package gighub.worketserver.auth;

import gighub.worketserver.auth.token.TokenProvider;
import gighub.worketserver.auth.token.TokenService;
import gighub.worketserver.user.constants.Provider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;
    private final TokenService tokenService;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

        String registrationId = oauthToken.getAuthorizedClientRegistrationId();
        Provider provider;
        switch (registrationId.toLowerCase()) {
            case "kakao" -> provider = Provider.KAKAO;
            case "google" -> provider = Provider.GOOGLE;
            case "naver" -> provider = Provider.NAVER;
            default -> throw new IllegalArgumentException("지원하지 않는 OAuth provider: " + registrationId);
        }
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                registrationId,                  // providerId (동적)
                authentication.getName()         // 현재 로그인한 사용자 식별자
        );

        String oauthAccessToken = client.getAccessToken().getTokenValue();

        // oauth access token을 DB나 Redis 등에 저장함
        tokenService.saveOauthAccessToken(authentication.getName(), oauthAccessToken, provider);

        // 로그인 성공 시 JWT 발급
        String accessToken = tokenProvider.generateAccessToken(authentication);
        tokenProvider.generateRefreshToken(authentication, accessToken);

        String redirectUrl = "http://localhost:3000/oauth/callback?accessToken=" + accessToken;

        response.sendRedirect(redirectUrl);
    }
}
