package gighub.worketserver.global.filter;

import gighub.worketserver.domain.User;
import gighub.worketserver.domain.constants.Role;
import gighub.worketserver.global.exception.PasscodeErrorCode;
import gighub.worketserver.global.exception.PasscodeException;
import gighub.worketserver.global.exception.PasscodeForFilterErrorCode;
import gighub.worketserver.global.exception.PasscodeForFilterException;
import gighub.worketserver.global.security.dto.PrincipalDetails;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
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
    String method = request.getMethod();

    // 1. 기본적으로 필터를 적용하지 않을 경로들
    if (uri.startsWith("/test")
      || uri.startsWith("/oauth2")
      || uri.startsWith("/auth/passcode")
      || uri.matches("^/transactions/\\d+/preview$")) {
      return true;
    }

    // 2. /users/me 중에서도 POST만 예외 (회원가입 단계라 패스코드 없어야 한다)
    if (uri.equals("/users/me") && method.equals("POST")) {
      return true;  // 필터 스킵
    }

    // 3. 그 외 GET /users/me 등은 필터 적용
    return false;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain)
    throws ServletException, IOException {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    PrincipalDetails details = (PrincipalDetails) auth.getPrincipal();
    User user = details.getUser();

    Role role = user.getRole();

    if (user.getPasscode() == null || user.getPasscode().isBlank()) {

      PasscodeForFilterErrorCode errorCode = switch (role) {
        case FREELANCER -> PasscodeForFilterErrorCode.PASSCODE_EMPTY_FREELANCER;
        case CLIENT -> PasscodeForFilterErrorCode.PASSCODE_EMPTY_CLIENT;
        default -> PasscodeForFilterErrorCode.PASSCODE_EMPTY;
      };
      request.setAttribute("exception", errorCode);
      throw new PasscodeForFilterException(errorCode);

    }

    filterChain.doFilter(request, response);
  }
}
