package gighub.worketserver.global.security;

import gighub.worketserver.global.security.token.TokenProvider;
import gighub.worketserver.global.security.token.TokenService;
import gighub.worketserver.global.security.token.constants.Provider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
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
    System.out.println(oauthToken);

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
    String refreshToken = tokenProvider.generateRefreshToken(authentication, accessToken);

    ResponseCookie cookie = ResponseCookie.from("accessToken", accessToken)
      .httpOnly(true)
      .secure(false) // https
      .sameSite("Lax")
      .path("/")
      .maxAge(60 * 60)
      .build();

    ResponseCookie refreshCookie = ResponseCookie.from("refreshToken", refreshToken)
      .httpOnly(true)
      .secure(false)
      .sameSite("Lax")
      .path("/")
      .maxAge(7 * 24 * 60 * 60)
      .build();

    response.addHeader("Set-Cookie", cookie.toString());
    response.addHeader("Set-Cookie", refreshCookie.toString());

    response.sendRedirect("http://localhost:3000");
  }
}
