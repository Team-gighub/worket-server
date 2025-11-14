package gighub.worketserver.global.util;

import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

  public ResponseCookie createTokenCookie(String name, String value, long maxAgeSeconds) {
    return ResponseCookie.from(name, value)
      .httpOnly(true)
      .secure(false)
      .sameSite("Lax")
      .path("/")
      .maxAge(maxAgeSeconds)
      .build();
  }
}
