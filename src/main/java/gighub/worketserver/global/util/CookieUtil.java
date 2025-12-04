package gighub.worketserver.global.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

  @Value("${COOKIE_DOMAIN:}")
  private String cookieDomain;

  @Value("${COOKIE_SAME_SITE:Lax}")
  private String sameSite;

  @Value("${COOKIE_SECURE:false}")
  private boolean secure;

  public ResponseCookie createTokenCookie(String name, String value, long maxAgeSeconds) {
    String domainToSet = cookieDomain.isBlank() ? null : cookieDomain;
    return ResponseCookie.from(name, value)
      .httpOnly(true)
      .secure(secure)
      .sameSite(sameSite)
      .path("/")
      .domain(domainToSet)
      .maxAge(maxAgeSeconds)
      .build();
  }
}
