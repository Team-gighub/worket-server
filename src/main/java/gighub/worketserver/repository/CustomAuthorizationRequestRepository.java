package gighub.worketserver.repository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CustomAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

  private final Map<String, String> stateStore = new ConcurrentHashMap<>();

  @Override
  public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
    return (OAuth2AuthorizationRequest) request.getSession().getAttribute("OAUTH2_AUTH_REQUEST");
  }

  @Override
  public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
                                       HttpServletRequest request,
                                       HttpServletResponse response) {
    if (authorizationRequest == null) {
      removeAuthorizationRequest(request, response);
      return;
    }

    request.getSession().setAttribute("OAUTH2_AUTH_REQUEST", authorizationRequest);
    String state = authorizationRequest.getState();
    stateStore.put(request.getSession().getId(), state);
  }

  @Override
  public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request,
                                                               HttpServletResponse response) {
    OAuth2AuthorizationRequest req =
      (OAuth2AuthorizationRequest) request.getSession().getAttribute("OAUTH2_AUTH_REQUEST");
    request.getSession().removeAttribute("OAUTH2_AUTH_REQUEST");
    return req;
  }

  public String getSavedState(HttpServletRequest request) {
    return stateStore.get(request.getSession().getId());
  }
}

