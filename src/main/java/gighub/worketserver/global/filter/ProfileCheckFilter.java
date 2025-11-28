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

  protected boolean shouldNotFilter(HttpServletRequest request) {
    String uri = request.getRequestURI();
    String method = request.getMethod();

    // 1. 기본적으로 필터를 적용하지 않을 경로들
    if (uri.startsWith("/test")
      || uri.startsWith("/oauth2")
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
