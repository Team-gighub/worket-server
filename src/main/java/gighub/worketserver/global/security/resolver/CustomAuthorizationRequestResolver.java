package gighub.worketserver.global.security.resolver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CustomAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

  private final OAuth2AuthorizationRequestResolver defaultResolver;

  public CustomAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
    this.defaultResolver =
      new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request) {
    OAuth2AuthorizationRequest req = defaultResolver.resolve(request);
    return customizeAuthorizationRequest(request, req);
  }

  @Override
  public OAuth2AuthorizationRequest resolve(HttpServletRequest request, String clientRegistrationId) {
    OAuth2AuthorizationRequest req = defaultResolver.resolve(request, clientRegistrationId);
    return customizeAuthorizationRequest(request, req);
  }

  private OAuth2AuthorizationRequest customizeAuthorizationRequest(HttpServletRequest request,
                                                                   OAuth2AuthorizationRequest req) {
    if (req == null) return null;

    // 프론트에서 전달한 state 파라미터 읽기
    String state = request.getParameter("state");

    // state가 없으면 기본값 FREELANCER 설정
    if (!StringUtils.hasText(state)) {
      state = "FREELANCER";
    }

    // AuthorizationRequest의 state 필드에 직접 주입 (이게 핵심)
    return OAuth2AuthorizationRequest.from(req)
      .state(state)
      .build();
  }
}
