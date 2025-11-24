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
    return uri.startsWith("/test")
      || uri.startsWith("/oauth2")
      || uri.startsWith("/auth/passcode")
      || uri.startsWith("/users/me")
      || uri.matches("^/transactions/\\d+/preview$");
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
