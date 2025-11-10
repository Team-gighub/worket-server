package gighub.worketserver.global.filter;

import gighub.worketserver.global.security.token.TokenProvider;
import gighub.worketserver.global.exception.TokenException;
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

    String accessToken = resolveCookieValue(request, "accessToken");
    String refreshToken = resolveCookieValue(request, "refreshToken");

    try {
      // 1. access token 유효 → 인증 설정
      if (StringUtils.hasText(accessToken) && tokenProvider.validateToken(accessToken)) {
        setAuthentication(accessToken);
      }

      // 2. access token 만료 → refresh token 검증 후 재발급
      else if (StringUtils.hasText(refreshToken) && tokenProvider.validateToken(refreshToken)) {
        System.out.println("프로바이더" + tokenProvider.validateToken(refreshToken));
        System.out.println(accessToken);
        System.out.println(refreshToken);
          String newAccessToken = tokenProvider.reissueAccessToken(refreshToken);
        if (StringUtils.hasText(newAccessToken)) {
          setAuthentication(newAccessToken);

          // 새 access token 쿠키 재설정
          addCookie(response, "accessToken", newAccessToken, 60 * 30);

          // refresh token도 갱신
          Authentication auth = tokenProvider.getAuthentication(newAccessToken);
          String newRefreshToken = tokenProvider.generateRefreshToken(auth);
          addCookie(response, "refreshToken", newRefreshToken, 7 * 24 * 60 * 60);
        }
      }

      // 3. 두 토큰 모두 유효하지 않음
      else {
        System.out.println("비웁니다;");
        clearCookies(response);
      }

    } catch (TokenException e) {
      log.warn("토큰 검증 실패: {}", e.getMessage());
      clearCookies(response);
    } catch (Exception e) {
      log.error("TokenAuthenticationFilter 처리 중 오류", e);
      clearCookies(response);
    }

    filterChain.doFilter(request, response);
  }

  private void setAuthentication(String token) {
    Authentication authentication = tokenProvider.getAuthentication(token);
    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private String resolveCookieValue(HttpServletRequest request, String name) {
    if (request.getCookies() == null) return null;
    for (Cookie cookie : request.getCookies()) {
      if (cookie.getName().equals(name)) {
        String value = cookie.getValue();
        // 쿠키 값이 빈 문자열("")이면 null로 처리해서 refreshToken 분기로 진입시키기
        return (StringUtils.hasText(value)) ? value : null;
      }
    }
    return null;
  }

  private void addCookie(HttpServletResponse response, String name, String value, long maxAgeSeconds) {
    ResponseCookie cookie = ResponseCookie.from(name, value)
      .httpOnly(true)
      .secure(false) // 운영 시 true
      .sameSite("Lax")
      .path("/")
      .maxAge(maxAgeSeconds)
      .build();
    response.addHeader("Set-Cookie", cookie.toString());
  }

  private void clearCookies(HttpServletResponse response) {
    ResponseCookie clearAccess = ResponseCookie.from("accessToken", "")
      .path("/")
      .maxAge(0)
      .build();
    ResponseCookie clearRefresh = ResponseCookie.from("refreshToken", "")
      .path("/")
      .maxAge(0)
      .build();
    response.addHeader("Set-Cookie", clearAccess.toString());
    response.addHeader("Set-Cookie", clearRefresh.toString());
  }
}
