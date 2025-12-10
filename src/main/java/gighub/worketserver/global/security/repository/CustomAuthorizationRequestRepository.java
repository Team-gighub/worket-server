package gighub.worketserver.global.security.repository;

import gighub.worketserver.global.util.StateCookieUtil;
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

  private final StateCookieUtil stateCookieUtil;

  private static final String AUTH_REQUEST_SESSION_KEY = "OAUTH2_AUTH_REQUEST";
  private static final String STATE_COOKIE_NAME = "oauth_state";
  private static final int COOKIE_EXPIRE_SECONDS = 180;

  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    return (OAuth2AuthorizationRequest)
      request.getSession().getAttribute(AUTH_REQUEST_SESSION_KEY);
  }

  @Override
  public void saveAuthorizationRequest(
    OAuth2AuthorizationRequest authorizationRequest,
    HttpServletRequest request,
    HttpServletResponse response
  ) {
    if (authorizationRequest == null) {
      removeAuthorizationRequest(request, response);
      return;
    }

    // 1) AuthorizationRequest 세션에 저장 (원래 네 방식 그대로)
    request.getSession().setAttribute(AUTH_REQUEST_SESSION_KEY, authorizationRequest);

    // 2) state를 Lax 쿠키로 저장
    String state = authorizationRequest.getState();
    var cookie = stateCookieUtil.createStateCookie(
      STATE_COOKIE_NAME,
      state,
      COOKIE_EXPIRE_SECONDS
    );

    response.addHeader("Set-Cookie", cookie.toString());
  }

  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(
    HttpServletRequest request,
    HttpServletResponse response
  ) {
    OAuth2AuthorizationRequest req = loadAuthorizationRequest(request);

    request.getSession().removeAttribute(AUTH_REQUEST_SESSION_KEY);

    var deleteCookie = stateCookieUtil.deleteStateCookie(STATE_COOKIE_NAME);
    response.addHeader("Set-Cookie", deleteCookie.toString());

    return req;
  }
  
  public String getSavedState(HttpServletRequest request) {
    if (request.getCookies() == null) return null;

    for (Cookie cookie : request.getCookies()) {
      if (STATE_COOKIE_NAME.equals(cookie.getName())) {
        return cookie.getValue();
      }
    }
    return null;
  }

}
