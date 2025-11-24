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

    // 1. Passcode 필터 무시해야 하는 공통 예외
    if (uri.startsWith("/test") ||
      uri.startsWith("/oauth2") ||
      uri.startsWith("/auth") ||       // 패스코드 등록/검증 포함
      uri.matches("^/transactions/\\d+/preview$")) {
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

    if (user.getPasscode() == null || user.getPasscode().isBlank()) {
      request.setAttribute("exception", PasscodeErrorCode.PASSCODE_EMPTY);
      throw new PasscodeException(PasscodeErrorCode.PASSCODE_EMPTY);
    }

    filterChain.doFilter(request, response);
  }
}
