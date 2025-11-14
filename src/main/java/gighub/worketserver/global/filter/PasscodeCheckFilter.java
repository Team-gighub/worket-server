package gighub.worketserver.global.filter;

import gighub.worketserver.domain.User;
import gighub.worketserver.global.exception.PasscodeErrorCode;
import gighub.worketserver.global.exception.PasscodeException;
import gighub.worketserver.global.security.dto.PrincipalDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class PasscodeCheckFilter extends OncePerRequestFilter {

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String uri = request.getRequestURI();

    // 로그인/토큰 재발급/공개 API 는 패스코드 검사 제외
    if (uri.startsWith("/oauth2")
      || uri.startsWith("/auth")
      || uri.startsWith("/test")) {
      return true;
    }

    return false;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
    throws ServletException, IOException {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    if (auth != null && auth.isAuthenticated()) {

      Object principal = auth.getPrincipal();
      User user;

      if (principal instanceof PrincipalDetails details) {
        user = details.getUser();
      } else {
        throw new PasscodeException(PasscodeErrorCode.PASSCODE_EMPTY);
      }

      if (user.getPasscode() == null || user.getPasscode().isBlank()) {
        throw new PasscodeException(PasscodeErrorCode.PASSCODE_EMPTY);
      }
    }

    filterChain.doFilter(request, response);
  }
}
