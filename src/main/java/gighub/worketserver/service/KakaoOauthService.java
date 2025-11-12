package gighub.worketserver.service;

import gighub.worketserver.global.security.token.TokenProvider;
import gighub.worketserver.domain.constants.Provider;
import gighub.worketserver.domain.constants.Status;
import gighub.worketserver.global.util.CookieUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOauthService {

  private final TokenProvider tokenProvider;
  private final OauthTokenService oauthTokenService;
  private final RefreshTokenService refreshTokenService;
  private final UserService userService;
  private final RestTemplate restTemplate;
  private final CookieUtil cookieUtil;

  /** 카카오 로그아웃 */
  public ResponseEntity<String> logout(HttpServletRequest request) {
    try {
      Long userId = extractUserIdFromJwt(request);
      String kakaoAccessToken = getKakaoAccessToken(userId);

      // 1. 카카오 로그아웃 요청
      callKakaoApi("https://kapi.kakao.com/v1/user/logout", kakaoAccessToken);

      // 2. 내부 JWT RefreshToken revoke
      refreshTokenService.revokeAllRefreshTokens(userId);

      // 3. 쿠키 삭제
      ResponseCookie clearAccessToken = cookieUtil.createTokenCookie("accessToken", "", 0);
      ResponseCookie clearRefreshToken = cookieUtil.createTokenCookie("refreshToken", "", 0);

      return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, clearAccessToken.toString(), clearRefreshToken.toString())
        .body("로그아웃 완료");

    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (RestClientException e) {
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body("카카오 서버와 통신 중 오류가 발생했습니다.");
    } catch (Exception e) {
      return ResponseEntity.internalServerError().body("로그아웃 중 오류가 발생했습니다.");
    }
  }

  /** 카카오 연결 해제 */
  public ResponseEntity<String> unlink(HttpServletRequest request) {
    try {
      Long userId = extractUserIdFromJwt(request);
      String kakaoAccessToken = getKakaoAccessToken(userId);

      // 1. 카카오 계정 연결 해제 API 호출
      callKakaoApi("https://kapi.kakao.com/v1/user/unlink", kakaoAccessToken);

      // 2. 외부 OAuth 토큰 삭제
      oauthTokenService.deleteOauthAccessToken(userId, Provider.KAKAO);

      // 3. 내부 RefreshToken 모두 revoke
      refreshTokenService.revokeAllRefreshTokens(userId);

      // 4. 사용자 상태 변경
      userService.updateUserStatus(userId, Status.DELETED);

      log.info("사용자 {}의 카카오 연동 해제 완료", userId);

      // 5. 쿠키 삭제
      ResponseCookie clearAccessToken = cookieUtil.createTokenCookie("accessToken", "", 0);
      ResponseCookie clearRefreshToken = cookieUtil.createTokenCookie("refreshToken", "", 0);


      return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, clearAccessToken.toString(), clearRefreshToken.toString())
        .body("카카오 계정 연결이 해제되었습니다.");

    } catch (IllegalArgumentException e) {
      log.error("연동 해제 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (RestClientException e) {
      log.error("카카오 API 호출 실패", e);
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body("카카오 서버와 통신 중 오류가 발생했습니다.");
    } catch (Exception e) {
      log.error("연동 해제 중 오류", e);
      return ResponseEntity.internalServerError().body("연동 해제 중 오류가 발생했습니다.");
    }
  }

  /** JWT 쿠키에서 사용자 ID 추출 */
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
    if (jwt == null) throw new IllegalArgumentException("JWT 토큰이 없습니다.");

    Authentication auth = tokenProvider.getAuthentication(jwt);
    return Long.parseLong(auth.getName());
  }

  /** DB에서 Kakao Access Token 조회 */
  private String getKakaoAccessToken(Long userId) {
    String token = oauthTokenService.findOauthAccessToken(userId, Provider.KAKAO);
    if (token == null)
      throw new IllegalArgumentException("카카오 access token이 없습니다.");
    return token;
  }

  /** 카카오 API 호출 공통 메서드 */
  private void callKakaoApi(String url, String kakaoAccessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + kakaoAccessToken);
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    HttpEntity<String> entity = new HttpEntity<>(null, headers);
    restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
  }
}
