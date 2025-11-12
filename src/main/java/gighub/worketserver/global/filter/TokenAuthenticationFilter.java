package gighub.worketserver.global.filter;

import gighub.worketserver.global.security.token.TokenProvider;
import gighub.worketserver.global.exception.TokenException;
import gighub.worketserver.global.util.CookieUtil;
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
  private final RefreshTokenService refreshTokenService;
  private final CookieUtil cookieUtil;

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

            ResponseCookie newAccessCookie =
              cookieUtil.createTokenCookie("accessToken", newAccessToken, 60 * 30);
            response.addHeader("Set-Cookie", newAccessCookie.toString());
          }

          // refresh token 만료 임박 여부 확인 후 재발급 (잔여기간 1일 이하)
          if (refreshTokenService.isExpiringSoon(refreshToken, 24)) {
            String newRefreshToken = tokenProvider.reissueRefreshToken(refreshToken);

            ResponseCookie refreshCookie = cookieUtil.createTokenCookie("refreshToken", newRefreshToken, 7 * 24 * 60 * 60);
            response.addHeader("Set-Cookie", refreshCookie.toString());
            System.out.println("재발급 제발 됐어라");
          }
        }  // 3. 두 토큰 모두 유효하지 않음
      } else {
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

  private void clearCookies(HttpServletResponse response) {
    ResponseCookie clearAccess = cookieUtil.createTokenCookie("accessToken", "", 0);
    ResponseCookie clearRefresh = cookieUtil.createTokenCookie("refreshToken", "", 0);

    response.addHeader("Set-Cookie", clearAccess.toString());
    response.addHeader("Set-Cookie", clearRefresh.toString());
  }
}
