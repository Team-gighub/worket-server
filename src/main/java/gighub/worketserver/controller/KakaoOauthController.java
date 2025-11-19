package gighub.worketserver.controller;

import gighub.worketserver.global.exception.TokenException;
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
    try {
      kakaoOauthService.logout(request, response);
      return ApiResponse.ok("로그아웃 완료");
    } catch (TokenException e) {
      var c = e.getErrorCode();
      return ApiResponse.error(c.getMessage(), c.getCustomCode());
    } catch (Exception e) {
      return ApiResponse.error("로그아웃 중 서버 오류가 발생했습니다.", "SERVER_5000");
    }
  }

  @PostMapping("/unlink")
  public ApiResponse<String> unlink(HttpServletRequest request, HttpServletResponse response) {
    try {
      kakaoOauthService.unlink(request, response);
      return ApiResponse.ok("카카오 계정 연결 해제 완료");
    } catch (TokenException e) {
      var c = e.getErrorCode();
      return ApiResponse.error(c.getMessage(), c.getCustomCode());
    } catch (Exception e) {
      return ApiResponse.error("연동 해제 중 서버 오류가 발생했습니다.", "SERVER_5000");
    }
  }

  @PostMapping("/token/refresh")
  public ApiResponse<?> refreshKakaoAccessToken(@RequestParam Long userId) {
    try {
      String newAccess = kakaoTokenRefreshService.refreshKakaoAccessToken(userId);
      return ApiResponse.ok(
        Map.of("message", "카카오 access token 재발급 완료", "accessToken", newAccess)
      );
    } catch (TokenException e) {
      var c = e.getErrorCode();
      return ApiResponse.error(c.getMessage(), c.getCustomCode());
    } catch (Exception e) {
      return ApiResponse.error("카카오 토큰 재발급 중 서버 오류가 발생했습니다.", "SERVER_5000");
    }
  }
}
