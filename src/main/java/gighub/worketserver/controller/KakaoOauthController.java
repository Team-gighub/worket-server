package gighub.worketserver.controller;

import gighub.worketserver.global.security.token.TokenProvider;
import gighub.worketserver.global.security.token.TokenService;

import gighub.worketserver.domain.constants.Status;
import gighub.worketserver.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/kakao")
public class KakaoOauthController {

  private final TokenProvider tokenProvider;
  private final TokenService tokenService;
  private final UserService userService;
  private final RestTemplate restTemplate = new RestTemplate();

  /** 카카오 로그아웃 */
  @PostMapping("/logout")
  public ResponseEntity<String> logoutKakao(HttpServletRequest request) {
    try {
      String userId = extractUserIdFromJwt(request);
      String kakaoAccessToken = getKakaoAccessToken(userId);

      ResponseEntity<String> kakaoResponse = callKakaoApi(
        "https://kapi.kakao.com/v1/user/logout", kakaoAccessToken);

      if (kakaoResponse.getStatusCode().is2xxSuccessful()) {
        // 로그아웃은 토큰만 삭제
        tokenService.deleteOauthAccessToken(userId);
        return ResponseEntity.ok("카카오 로그아웃 완료");
      } else {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
          .body("카카오 로그아웃 실패: " + kakaoResponse.getBody());
      }

    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
        .body("로그아웃 처리 중 오류: " + e.getMessage());
    }
  }

  /** 카카오 계정 연결 해제 (unlink) */
  @PostMapping("/unlink")
  public ResponseEntity<String> unlinkKakao(HttpServletRequest request) {
    try {
      String userId = extractUserIdFromJwt(request);
      String kakaoAccessToken = getKakaoAccessToken(userId);

      ResponseEntity<String> kakaoResponse = callKakaoApi(
        "https://kapi.kakao.com/v1/user/unlink", kakaoAccessToken);

      if (kakaoResponse.getStatusCode().is2xxSuccessful()) {
        // unlink는 DB 상태 변경 + 모든 토큰 삭제
        tokenService.deleteOauthAccessToken(userId);
        tokenService.deleteRefreshToken(userId);
        userService.updateUserStatus(userId, Status.DELETED);

        return ResponseEntity.ok("카카오 계정 연결이 해제되었습니다.");
      } else {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
          .body("카카오 연동 해제 실패: " + kakaoResponse.getBody());
      }

    } catch (IllegalArgumentException e) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
    } catch (Exception e) {
      return ResponseEntity.internalServerError()
        .body("연동 해제 중 오류: " + e.getMessage());
    }
  }

  private String extractUserIdFromJwt(HttpServletRequest request) {
    String jwt = null;

    if (jwt == null && request.getCookies() != null) {
      for (var cookie : request.getCookies()) {
        if ("accessToken".equals(cookie.getName())) {
          jwt = cookie.getValue();
          break;
        }
      }
    }

    if (jwt == null) {
      throw new IllegalArgumentException("JWT 토큰이 없습니다. 로그인 후 다시 시도하세요.");
    }

    Authentication authentication = tokenProvider.getAuthentication(jwt);
    return authentication.getName();
  }

  /** Oauth Access Token 조회 및 검증 */
  private String getKakaoAccessToken(String userId) {
    String kakaoAccessToken = tokenService.findOauthAccessToken(userId);
    if (kakaoAccessToken == null) {
      throw new IllegalArgumentException("카카오 access token이 없습니다. 다시 로그인해주세요.");
    }
    return kakaoAccessToken;
  }

  /** 카카오 API 호출 공통 처리 */
  private ResponseEntity<String> callKakaoApi(String url, String kakaoAccessToken) {
    HttpHeaders headers = new HttpHeaders();
    headers.set("Authorization", "Bearer " + kakaoAccessToken);
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
    HttpEntity<String> entity = new HttpEntity<>(null, headers);

    return restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
  }
}
