package gighub.worketserver.global.filter;

import gighub.worketserver.domain.FreelancerProfile;
import gighub.worketserver.domain.User;
import gighub.worketserver.domain.constants.Role;
import gighub.worketserver.global.exception.ProfileErrorCode;
import gighub.worketserver.global.exception.ProfileException;
import gighub.worketserver.global.security.dto.PrincipalDetails;
import gighub.worketserver.repository.FreelancerProfileRepository;
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
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ProfileCheckFilter extends OncePerRequestFilter {
  private final FreelancerProfileRepository freelancerProfileRepository;

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String uri = request.getRequestURI();
    return uri.startsWith("/test")
      || uri.startsWith("/oauth2")
      || uri.startsWith("/auth")
      || uri.startsWith("/auth/passcode")
      || uri.matches("^/transactions/\\d+/preview$");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain)
    throws ServletException, IOException {

    Authentication auth = SecurityContextHolder.getContext().getAuthentication();

    // 로그인 성공한 사용자만 프로필 체크
    if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof PrincipalDetails details) {

      User user = details.getUser();

      // 프리랜서 프로필 체크
      if (user.getRole() == Role.FREELANCER) {
        Long userId = user.getId();
        Optional<FreelancerProfile> profileOpt = freelancerProfileRepository.findByUserId(userId);

        if (profileOpt.isEmpty()) {
          request.setAttribute("exception", ProfileErrorCode.FREELANCER_PROFILE_NOT_FOUND);
          throw new ProfileException(ProfileErrorCode.FREELANCER_PROFILE_NOT_FOUND);
        }
      }

      filterChain.doFilter(request, response);
    }
  }
}
