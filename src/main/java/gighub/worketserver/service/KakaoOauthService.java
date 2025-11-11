package gighub.worketserver.service;

import gighub.worketserver.global.security.token.TokenProvider;
import gighub.worketserver.domain.constants.Status;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoOauthService {

  private final TokenProvider tokenProvider;
  private final TokenService tokenService;
  private final UserService userService;
  private final RestTemplate restTemplate;

  public ResponseEntity<String> logout(HttpServletRequest request) {
    try {
      String userId = extractUserIdFromJwt(request);
      String kakaoAccessToken = getKakaoAccessToken(userId);

      callKakaoApi("https://kapi.kakao.com/v1/user/logout", kakaoAccessToken);

      tokenService.deleteRefreshToken(userId);

      ResponseCookie clearAccess = ResponseCookie.from("accessToken", "")
        .httpOnly(true)
        .secure(false) // 운영 시 true (https 필수)
        .sameSite("Lax")
        .path("/")
        .maxAge(0)
        .build();

      ResponseCookie clearRefresh = ResponseCookie.from("refreshToken", "")
        .httpOnly(true)
        .secure(false)
        .sameSite("Lax")
        .path("/")
        .maxAge(0)
        .build();

      return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, clearAccess.toString(), clearRefresh.toString())
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

  public ResponseEntity<String> unlink(HttpServletRequest request) {
    try {
      String userId = extractUserIdFromJwt(request);
      String kakaoAccessToken = getKakaoAccessToken(userId);

      callKakaoApi("https://kapi.kakao.com/v1/user/unlink", kakaoAccessToken);
      tokenService.deleteOauthAccessToken(userId);
      tokenService.deleteRefreshToken(userId);
      userService.updateUserStatus(userId, Status.DELETED);

      log.info("사용자 {}의 카카오 연동 해제 완료", userId);
      return ResponseEntity.ok("카카오 계정 연결이 해제되었습니다.");

    } catch (IllegalArgumentException e) {
      log.error("연동 해제 실패: {}", e.getMessage());
      return ResponseEntity.badRequest().body(e.getMessage());
    } catch (RestClientException e) {
      log.error("카카오 API 호출 실패", e);
      return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
        .body("카카오 서버와 통신 중 오류가 발생했습니다.");
    } catch (Exception e) {
      log.error("연동 해제 중 예상치 못한 오류", e);
      return ResponseEntity.internalServerError().body("연동 해제 중 오류가 발생했습니다.");
    }
  }

  private String extractUserIdFromJwt(HttpServletRequest request) {
    String jwt = null;

    if (request.getCookies() != null) {
      for (Cookie cookie : request.getCookies()) {
        if ("accessToken".equals(cookie.getName())) {
          jwt = cookie.getValue();
          break;
        }
      }
    }

    if (jwt == null) {
      throw new IllegalArgumentException("JWT 토큰이 없습니다.");
    }

    Authentication auth = tokenProvider.getAuthentication(jwt);
    return auth.getName();
  }

  private String getKakaoAccessToken(String userId) {
    String token = tokenService.findOauthAccessToken(userId);
    if (token == null) {
      throw new IllegalArgumentException("카카오 access token이 없습니다.");
    }
    return token;
  }

  private void callKakaoApi(String url, String kakaoAccessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + kakaoAccessToken);
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    HttpEntity<String> entity = new HttpEntity<>(null, headers);
    restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
  }
}
