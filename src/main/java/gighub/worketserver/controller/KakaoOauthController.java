package gighub.worketserver.controller;

import gighub.worketserver.global.response.ApiResponse;
import gighub.worketserver.service.KakaoOauthService;
import gighub.worketserver.service.KakaoTokenRefreshService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class KakaoOauthController {

  private final KakaoOauthService kakaoOauthService;
  private final KakaoTokenRefreshService kakaoTokenRefreshService;

  @PostMapping("/logout")
  public ApiResponse<String> logout(HttpServletRequest request, HttpServletResponse response) {
    kakaoOauthService.logout(request, response);
    return ApiResponse.ok("로그아웃 완료");
  }

  @PostMapping("/unlink")
  public ApiResponse<String> unlink(HttpServletRequest request, HttpServletResponse response) {
    kakaoOauthService.unlink(request, response);
    return ApiResponse.ok("카카오 계정 연결 해제 완료");
  }

  @PostMapping("/token/refresh")
  public ApiResponse<?> refreshKakaoAccessToken(@RequestParam Long userId) {
    String newAccess = kakaoTokenRefreshService.refreshKakaoAccessToken(userId);
    return ApiResponse.ok(
      Map.of("message", "카카오 access token 재발급 완료",
        "accessToken", newAccess)
    );
  }
}
