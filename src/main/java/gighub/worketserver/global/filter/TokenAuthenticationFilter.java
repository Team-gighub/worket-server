package gighub.worketserver.global.filter;

import gighub.worketserver.global.security.token.TokenProvider;
import gighub.worketserver.global.exception.TokenException;
import gighub.worketserver.service.RefreshTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Component
public class TokenAuthenticationFilter extends OncePerRequestFilter {

  private final TokenProvider tokenProvider;
  private final RefreshTokenService refreshTokenService;  // 추가

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {

    String accessToken = resolveCookieValue(request, "accessToken");
    String refreshToken = resolveCookieValue(request, "refreshToken");

    try {
      if (StringUtils.hasText(accessToken) && tokenProvider.validateToken(accessToken)) {
        setAuthentication(accessToken);
      }
      else if (StringUtils.hasText(refreshToken)) { // accessToken 만료 또는 없음
        // refreshToken 유효성 + DB 검증
        if (tokenProvider.validateToken(refreshToken) && refreshTokenService.isValidRefreshToken(refreshToken)) {
          String newAccessToken = tokenProvider.reissueAccessToken(refreshToken);
          if (StringUtils.hasText(newAccessToken)) {
            setAuthentication(newAccessToken);
            addCookie(response, "accessToken", newAccessToken, 60 * 30);
          }
        } else {
          clearCookies(response);
        }
      }

      // 3. 두 토큰 모두 유효하지 않음
      else {
        clearCookies(response);
      }

    } catch (TokenException e) {
      log.error("토큰 검증 실패: {}", e.getMessage());
      clearCookies(response);
    } catch (Exception e) {
      log.error("인증 처리 중 오류 발생: {}", e.getMessage(), e);
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
