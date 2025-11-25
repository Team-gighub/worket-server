package gighub.worketserver.global.security.handler;

import gighub.worketserver.global.security.repository.CustomAuthorizationRequestRepository;
import gighub.worketserver.global.security.token.TokenProvider;
import gighub.worketserver.global.util.CookieUtil;
import gighub.worketserver.global.util.StateParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

  private final TokenProvider tokenProvider;
  private final CookieUtil cookieUtil;
  private final CustomAuthorizationRequestRepository customAuthorizationRequestRepository;

  @Value("${frontend.base-url}")
  private String baseFrontUrl;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException {

    // state 꺼내기
    String rawState = customAuthorizationRequestRepository.getSavedState(request);

    // state 파싱
    var parsed = StateParser.parse(rawState);

    String role = parsed.role();
    String transactionId = parsed.transactionId();

    String redirectUrl = baseFrontUrl;

    if ("client".equalsIgnoreCase(role) && transactionId != null) {
      redirectUrl = baseFrontUrl + "/trade/" + transactionId;
    } else if ("freelancer".equalsIgnoreCase(role)) {
      redirectUrl = baseFrontUrl;
    }

    // 로그인 성공 시 JWT 발급
    String accessToken = tokenProvider.generateAccessToken(authentication);
    String refreshToken = tokenProvider.generateRefreshToken(authentication);

    // Access Token 쿠키
    ResponseCookie accessCookie = cookieUtil.createTokenCookie("accessToken", accessToken, 60 * 60);
    // Refresh Token 쿠키
    ResponseCookie refreshCookie = cookieUtil.createTokenCookie("refreshToken", refreshToken, 7 * 24 * 60 * 60);

    response.addHeader("Set-Cookie", accessCookie.toString());
    response.addHeader("Set-Cookie", refreshCookie.toString());

    response.sendRedirect(redirectUrl);
  }
}
