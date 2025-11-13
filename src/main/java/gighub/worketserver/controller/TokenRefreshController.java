package gighub.worketserver.controller;

import gighub.worketserver.global.response.ApiResponse;
import gighub.worketserver.service.KakaoTokenRefreshService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth/token")
@RequiredArgsConstructor
public class TokenRefreshController {

  private final KakaoTokenRefreshService kakaoTokenRefreshService;

  /** 카카오 OAuth Access Token 갱신 */
  @GetMapping("/refresh")
  public ApiResponse<?> refreshKakaoAccessToken(@RequestParam Long userId) {
    try {
      String newAccessToken = kakaoTokenRefreshService.refreshKakaoAccessToken(userId);

      log.info("카카오 access token 갱신 성공 - userId: {}", userId);

      return ApiResponse.ok(
        Map.of(
          "message", "카카오 access token 재발급 완료",
          "accessToken", newAccessToken
        )
      );

    } catch (IllegalStateException e) {
      log.error("카카오 토큰 갱신 실패: {}", e.getMessage());

      return ApiResponse.error(e.getMessage());
    } catch (Exception e) {
      log.error("카카오 토큰 갱신 중 서버 오류: {}", e.getMessage());

      return ApiResponse.error("카카오 토큰 재발급 중 서버 오류가 발생했습니다.");
    }
  }
}
