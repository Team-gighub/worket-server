package gighub.worketserver.global.security.repository;

import gighub.worketserver.global.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CustomAuthorizationRequestRepository
  implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  private final CookieUtil cookieUtil;

  private static final String STATE_COOKIE = "oauth_state";
  private static final int STATE_COOKIE_MAX_AGE_SECONDS = 180;

  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    return (OAuth2AuthorizationRequest)
      request.getSession().getAttribute("OAUTH2_AUTH_REQUEST");
  }

  @Override
  public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
    if (authorizationRequest == null) {
      removeAuthorizationRequest(request, response);
      return;
    }

    // OAuth2 요청 세션에 저장
    request.getSession().setAttribute("OAUTH2_AUTH_REQUEST", authorizationRequest);

    // state를 쿠키에 저장
    String state = authorizationRequest.getState();

    var cookie = cookieUtil.createTokenCookie(
      STATE_COOKIE,
      state,
      STATE_COOKIE_MAX_AGE_SECONDS
    );

    response.addHeader("Set-Cookie", cookie.toString());
  }

  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                               HttpServletResponse response) {
    OAuth2AuthorizationRequest req =
      (OAuth2AuthorizationRequest) request.getSession().getAttribute("OAUTH2_AUTH_REQUEST");

    request.getSession().removeAttribute("OAUTH2_AUTH_REQUEST");

    // 쿠키 삭제
    var deleteCookie = cookieUtil.createTokenCookie(
      STATE_COOKIE,
      "",
      0
    );
    response.addHeader("Set-Cookie", deleteCookie.toString());

    return req;
  }

  public String getSavedState(HttpServletRequest request) {
    if (request.getCookies() == null) return null;

    for (Cookie cookie : request.getCookies()) {
      if (STATE_COOKIE.equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }
}
