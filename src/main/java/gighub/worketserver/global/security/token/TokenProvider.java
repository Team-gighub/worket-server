package gighub.worketserver.global.security.token;

import gighub.worketserver.global.exception.TokenException;
import gighub.worketserver.service.TokenService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.*;
import java.util.stream.Collectors;

import static gighub.worketserver.global.exception.TokenErrorCode.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class TokenProvider {

  private final TokenService tokenService;

  @Value("${JWT_SECRET_KEY}")
  private String key;

  private SecretKey secretKey;

  private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000L * 60 * 30;       // 30분
  private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 7; // 7일
  private static final String KEY_ROLE = "role";

  @PostConstruct
  private void setSecretKey() {
    secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(key));
  }

  /**
   * AccessToken 발급
   */
  public String generateAccessToken(Authentication authentication) {
    return generateToken(authentication, ACCESS_TOKEN_EXPIRE_TIME);
  }

  /**
   * RefreshToken 발급 및 저장
   */
  @Transactional
  public String generateRefreshToken(Authentication authentication) {
    String refreshToken = generateToken(authentication, REFRESH_TOKEN_EXPIRE_TIME);
    tokenService.saveOrUpdateRefreshToken(authentication.getName(), refreshToken);
    return refreshToken;
  }

  /**
   * JWT 생성 공통 로직
   */
  private String generateToken(Authentication authentication, long expireTime) {
    Date now = new Date();
    Date expiry = new Date(now.getTime() + expireTime);

    String authorities = authentication.getAuthorities().stream()
      .map(GrantedAuthority::getAuthority)
      .collect(Collectors.joining(","));

    return Jwts.builder()
      .subject(authentication.getName())     // sub: oauth_id
      .claim(KEY_ROLE, authorities)
      .issuedAt(now)
      .expiration(expiry)
      .signWith(secretKey, Jwts.SIG.HS512)
      .compact();
  }

  /**
   * JWT에서 Authentication 복원
   */
  public Authentication getAuthentication(String token) {
    Claims claims = parseClaims(token);
    List<SimpleGrantedAuthority> authorities = getAuthorities(claims);

    org.springframework.security.core.userdetails.User principal =
      new org.springframework.security.core.userdetails.User(
        claims.getSubject(), "", authorities);

    return new UsernamePasswordAuthenticationToken(principal, token, authorities);
  }

  private List<SimpleGrantedAuthority> getAuthorities(Claims claims) {
    String role = claims.get(KEY_ROLE).toString();
    return Collections.singletonList(new SimpleGrantedAuthority(role));
  }

  /**
   * RefreshToken으로 AccessToken 재발급
   */
  @Transactional
  public String reissueAccessToken(String refreshToken) {
    if (!StringUtils.hasText(refreshToken)) return null;
    if (!validateToken(refreshToken)) return null;

    Authentication authentication = getAuthentication(refreshToken);
    return generateAccessToken(authentication);
  }

  /**
   * 토큰 유효성 검증
   */
  public boolean validateToken(String token) {
    if (!StringUtils.hasText(token)) return false;
    Claims claims = parseClaims(token);
    return claims.getExpiration().after(new Date());
  }

  /**
   * JWT 파싱 및 예외 처리
   */
  private Claims parseClaims(String token) {
    try {
      return Jwts.parser()
        .verifyWith(secretKey)
        .build()
        .parseSignedClaims(token)
        .getPayload();
    } catch (ExpiredJwtException e) {
      log.warn("만료된 토큰입니다.");
      return e.getClaims();
    } catch (MalformedJwtException e) {
      throw new TokenException(INVALID_TOKEN);
    } catch (SecurityException e) {
      throw new TokenException(INVALID_JWT_SIGNATURE);
    }
  }
}
