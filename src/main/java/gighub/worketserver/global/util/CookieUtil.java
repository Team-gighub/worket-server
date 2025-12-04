package gighub.worketserver.global.util;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

  public ResponseCookie createTokenCookie(String name, String value, long maxAgeSeconds) {
    return ResponseCookie.from(name, value)
      .httpOnly(true)
      .secure(true)
      .sameSite("None")
      .path("/")
      .domain("worket.site")
      .maxAge(maxAgeSeconds)
      .build();
  }
}
