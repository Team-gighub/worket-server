package gighub.worketserver.controller;

import gighub.worketserver.global.response.ApiResponse;
import gighub.worketserver.service.KakaoOauthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class KakaoOauthController {

  private final KakaoOauthService kakaoOauthService;

  @PostMapping("/logout")
  public ApiResponse<String> logout(HttpServletRequest request, HttpServletResponse response) {
    return kakaoOauthService.logout(request, response);
  }

  @PostMapping("/unlink")
  public ApiResponse<String> unlink(HttpServletRequest request, HttpServletResponse response) {
    return kakaoOauthService.unlink(request, response);
  }
}
