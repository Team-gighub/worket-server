package gighub.worketserver.global.security.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;

import java.util.*;

public class KakaoTokenResponseConverter implements Converter<Map<String, Object>, OAuth2AccessTokenResponse> {

  @Override
  public OAuth2AccessTokenResponse convert(Map<String, Object> tokenResponseParameters) {
    String accessToken = (String) tokenResponseParameters.get("access_token");
    long expiresIn = ((Number) tokenResponseParameters.get("expires_in")).longValue();

    Set<String> scopes = Collections.emptySet();
    if (tokenResponseParameters.containsKey("scope")) {
      String scope = (String) tokenResponseParameters.get("scope");
      scopes = new HashSet<>(Arrays.asList(scope.split(" ")));
    }

    Map<String, Object> additionalParameters = new HashMap<>(tokenResponseParameters);

    return OAuth2AccessTokenResponse.withToken(accessToken)
      .tokenType(OAuth2AccessToken.TokenType.BEARER)
      .expiresIn(expiresIn)
      .scopes(scopes)
      .refreshToken((String) tokenResponseParameters.get("refresh_token"))
      .additionalParameters(additionalParameters)
      .build();
  }
}
