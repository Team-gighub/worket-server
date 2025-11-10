package gighub.worketserver.global.security.token;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

  private final TokenProvider tokenProvider;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    String accessToken = resolveTokenFromCookie(request);

    if (StringUtils.hasText(accessToken) && tokenProvider.validateToken(accessToken)) {
      setAuthentication(accessToken);
    } else {
      // 만료된 경우 재발급 시도
      String reissuedToken = tokenProvider.reissueAccessToken(accessToken);
      if (StringUtils.hasText(reissuedToken)) {
        setAuthentication(reissuedToken);

        // 새 토큰을 쿠키로 재설정
        var newCookie = ResponseCookie.from("accessToken", reissuedToken)
          .httpOnly(true)
          .secure(false) // 운영 시 true
          .sameSite("Lax")
          .path("/")
          .maxAge(60 * 60)
          .build();
        response.addHeader("Set-Cookie", newCookie.toString());
      }
    }

    filterChain.doFilter(request, response);
  }

  private void setAuthentication(String accessToken) {
    Authentication authentication = tokenProvider.getAuthentication(accessToken);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private String resolveTokenFromCookie(HttpServletRequest request) {
    if (request.getCookies() == null) return null;

    for (Cookie cookie : request.getCookies()) {
      if ("accessToken".equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }
}
