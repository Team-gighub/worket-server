package gighub.worketserver.auth;

import gighub.worketserver.auth.token.TokenProvider;
import gighub.worketserver.auth.token.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.core.Authentication;
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
        // 카카오 Access Token 추출
        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                "kakao",
                authentication.getName()
        );
        String kakaoAccessToken = client.getAccessToken().getTokenValue();

        // 카카오 access token을 DB나 Redis 등에 저장 (unlink 시 사용)
        tokenService.saveKakaoAccessToken(authentication.getName(), kakaoAccessToken);

        // JWT 발급
        String accessToken = tokenProvider.generateAccessToken(authentication);
        tokenProvider.generateRefreshToken(authentication, accessToken);

        String redirectUrl = "http://localhost:3000/oauth/callback?accessToken=" + accessToken;

        response.sendRedirect(redirectUrl);
    }
}
