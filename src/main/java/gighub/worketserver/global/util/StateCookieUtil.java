package gighub.worketserver.global.util;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class StateCookieUtil {

  private static final String DOMAIN = "worket.site";

  public ResponseCookie createStateCookie(String name, String value, long maxAgeSeconds) {
    return ResponseCookie.from(name, value)
      .httpOnly(true)
      .secure(true)
      .sameSite("Lax")     // ★ 핵심: AuthorizationRequest state는 Lax여야 정상 작동
      .path("/")
      .domain(DOMAIN)
      .maxAge(maxAgeSeconds)
      .build();
  }

  public ResponseCookie deleteStateCookie(String name) {
    return ResponseCookie.from(name, "")
      .httpOnly(true)
      .secure(true)
      .sameSite("Lax")
      .path("/")
      .domain(DOMAIN)
      .maxAge(0)
      .build();
  }
}
