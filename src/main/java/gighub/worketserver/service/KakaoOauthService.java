package gighub.worketserver.service;

import gighub.worketserver.global.exception.TokenErrorCode;
import gighub.worketserver.global.exception.TokenException;
import gighub.worketserver.global.security.token.TokenProvider;
import gighub.worketserver.domain.constants.Provider;
import gighub.worketserver.domain.constants.Status;
import gighub.worketserver.global.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.Cookie;

@Service
@RequiredArgsConstructor
public class KakaoOauthService {

    private final TokenProvider tokenProvider;
    private final OauthTokenService oauthTokenService;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;
    private final RestTemplate restTemplate;
    private final CookieUtil cookieUtil;

    public void logout(HttpServletRequest request, HttpServletResponse response) {

        Long userId = extractUserId(request);
        String kakaoAccess = getKakaoAccessToken(userId);

        callKakaoApi("https://kapi.kakao.com/v1/user/logout", kakaoAccess);

        refreshTokenService.revokeAllRefreshTokens(userId);

        ResponseCookie clearAccess = cookieUtil.createTokenCookie("accessToken", "", 0);
        ResponseCookie clearRefresh = cookieUtil.createTokenCookie("refreshToken", "", 0);

        response.addHeader(HttpHeaders.SET_COOKIE, clearAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefresh.toString());
    }

    public void unlink(HttpServletRequest request, HttpServletResponse response) {

        Long userId = extractUserId(request);
        String kakaoAccess = getKakaoAccessToken(userId);

        callKakaoApi("https://kapi.kakao.com/v1/user/unlink", kakaoAccess);

  /**
   * 카카오 연결 해제
   */
  public ApiResponse<String> unlink(HttpServletRequest request, HttpServletResponse response) {
    try {
      Long userId = extractUserIdFromJwt(request);
      String kakaoAccessToken = getKakaoAccessToken(userId);

        ResponseCookie clearAccess = cookieUtil.createTokenCookie("accessToken", "", 0);
        ResponseCookie clearRefresh = cookieUtil.createTokenCookie("refreshToken", "", 0);

        response.addHeader(HttpHeaders.SET_COOKIE, clearAccess.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, clearRefresh.toString());
    }

    private Long extractUserId(HttpServletRequest request) {

      // 4. 사용자 상태 변경
      userService.updateUserStatus(userId, Status.DELETED);

        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("accessToken".equals(c.getName())) {
                    jwt = c.getValue();
                    break;
                }
            }
        }

        if (jwt == null)
            throw new TokenException(TokenErrorCode.EMPTY_TOKEN);

        Authentication auth = tokenProvider.getAuthentication(jwt);
        return Long.parseLong(auth.getName());
    }

    private String getKakaoAccessToken(Long userId) {

        String token = oauthTokenService.findOauthAccessToken(userId, Provider.KAKAO);

        if (token == null)
            throw new TokenException(TokenErrorCode.INVALID_TOKEN);

        return token;
    }

    private void callKakaoApi(String url, String kakaoAccessToken) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + kakaoAccessToken);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        try {
            restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(null, headers), String.class);
        } catch (RestClientException e) {
            throw new TokenException(TokenErrorCode.INVALID_TOKEN);
  }

  /**
   * JWT 쿠키에서 사용자 ID 추출
   */
  private Long extractUserIdFromJwt(HttpServletRequest request) {
    String jwt = null;
    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("accessToken".equals(cookie.getName())) {
          jwt = cookie.getValue();
          break;
        }
    }
}
