package gighub.worketserver.controller;

import gighub.worketserver.service.KakaoOauthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class KakaoOauthController {

  private final KakaoOauthService kakaoOauthService;

  @PostMapping("/logout")
  public ResponseEntity<String> logout(HttpServletRequest request) {
    return kakaoOauthService.logout(request);
  }

  @PostMapping("/unlink")
  public ResponseEntity<String> unlink(HttpServletRequest request) {
    return kakaoOauthService.unlink(request);
  }
}
