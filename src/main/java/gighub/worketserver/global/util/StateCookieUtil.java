package gighub.worketserver.global.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class StateCookieUtil {

  @Value("${COOKIE_DOMAIN:}")
  private String cookieDomain;

  @Value("${COOKIE_SECURE:false}")
  private boolean secure;

  public ResponseCookie createStateCookie(String name, String value, long maxAgeSeconds) {
    String domainToSet = cookieDomain.isBlank() ? null : cookieDomain;
    return ResponseCookie.from(name, value)
      .httpOnly(true)
      .secure(secure)
      .sameSite("Lax")     // AuthorizationRequest state는 Lax여야 정상 작동
      .path("/")
      .domain(domainToSet)
      .maxAge(maxAgeSeconds)
      .build();
  }

  public ResponseCookie deleteStateCookie(String name) {
    return ResponseCookie.from(name, "")
      .httpOnly(true)
      .secure(secure)
      .sameSite("Lax")
      .path("/")
      .domain(cookieDomain)
      .maxAge(0)
      .build();
  }
}
