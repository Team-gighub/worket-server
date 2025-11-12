package gighub.worketserver.controller;

import gighub.worketserver.service.KakaoTokenRefreshService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth/token")
@RequiredArgsConstructor
public class TokenRefreshController {

  private final KakaoTokenRefreshService kakaoTokenRefreshService;

  /**
   * 카카오 OAuth Access Token 갱신
   * GET /auth/token/refresh?userId=1
   */
  @GetMapping("/refresh")
  public ResponseEntity<?> refreshKakaoAccessToken(@RequestParam Long userId) {
    try {
      String newAccessToken = kakaoTokenRefreshService.refreshKakaoAccessToken(userId);

      log.info("카카오 access token 갱신 성공 - userId: {}", userId);

      return ResponseEntity.ok(Map.of(
        "success", true,
        "message", "카카오 access token 재발급 완료",
        "accessToken", newAccessToken
      ));

    } catch (IllegalStateException e) {
      log.error("카카오 토큰 갱신 실패: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
        "success", false,
        "error", "TOKEN_EXPIRED",
        "message", e.getMessage(),
        "requireRelogin", true
      ));

    } catch (Exception e) {
      log.error("카카오 토큰 갱신 중 서버 오류: {}", e.getMessage());
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
        "success", false,
        "error", "SERVER_ERROR",
        "message", e.getMessage()
      ));
    }
  }
}
