package gighub.worketserver.auth.dto;

import gighub.worketserver.user.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 *  OAuth2 전용 Principal 클래스
 * - OAuth2 로그인(Kakao, Google 등) 사용자 정보를 Spring Security가 인식할 수 있게 감싸주는 역할
 * - 폼 로그인(UserDetails)은 사용하지 않음
 */
public class PrincipalDetails implements OAuth2User {

  private final User user;
  private final Map<String, Object> attributes;
  private final String attributeKey;

  public PrincipalDetails(User user, Map<String, Object> attributes, String attributeKey) {
    this.user = user;
    this.attributes = attributes;
    this.attributeKey = attributeKey;
  }

  @Override
  public String getName() {
    // provider에서 제공하는 기본 식별자(ex. "id")
    return attributes != null && attributeKey != null
      ? attributes.get(attributeKey).toString()
      : user.getName();
  }

  @Override
  public Map<String, Object> getAttributes() {
    return attributes;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(
      new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
    );
  }
}
