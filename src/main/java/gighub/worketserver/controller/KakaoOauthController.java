package gighub.worketserver.controller;

import gighub.worketserver.global.response.ApiResponse;
import gighub.worketserver.service.KakaoOauthService;
import gighub.worketserver.service.KakaoTokenRefreshService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class KakaoOauthController {

  private final KakaoOauthService kakaoOauthService;
  private final KakaoTokenRefreshService kakaoTokenRefreshService;

  @PostMapping("/logout")
  public ApiResponse<String> logout(HttpServletRequest request, HttpServletResponse response) {
    return kakaoOauthService.logout(request, response);
  }

  @PostMapping("/unlink")
  public ApiResponse<String> unlink(HttpServletRequest request, HttpServletResponse response) {
    return kakaoOauthService.unlink(request, response);
  }

  /** 카카오 OAuth Access Token 갱신 */
  @PostMapping("/token/refresh")
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
