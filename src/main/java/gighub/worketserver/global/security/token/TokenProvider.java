package gighub.worketserver.global.security.token;

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

import static gighub.worketserver.global.security.token.TokenErrorCode.*;

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
        // Base64 인코딩된 문자열이면 decode 처리
        secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(key));
    }

    /** AccessToken 발급 */
    public String generateAccessToken(Authentication authentication) {
        return generateToken(authentication, ACCESS_TOKEN_EXPIRE_TIME);
    }

    /** RefreshToken 발급 및 저장 */
    @Transactional
    public String generateRefreshToken(Authentication authentication, String accessToken) {
      String refreshToken = generateToken(authentication, REFRESH_TOKEN_EXPIRE_TIME);
      tokenService.saveOrUpdate(authentication.getName(), refreshToken, accessToken);
      return refreshToken;
    }

    /**  JWT 생성 로직 */
    private String generateToken(Authentication authentication, long expireTime) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireTime);

        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(authentication.getName()) //oauth_id
                .claim(KEY_ROLE, authorities)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    /**  토큰에서 Authentication 객체 복원 */
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);
        List<SimpleGrantedAuthority> authorities = getAuthorities(claims);

        // UserDetails 대신 PrincipalDetails를 써도 됨 (너의 인증 구조)
        org.springframework.security.core.userdetails.User principal =
                new org.springframework.security.core.userdetails.User(
                        claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    private List<SimpleGrantedAuthority> getAuthorities(Claims claims) {
        String role = claims.get(KEY_ROLE).toString();
        return Collections.singletonList(new SimpleGrantedAuthority(role));
    }

    /**  AccessToken 재발급 */
    @Transactional
    public String reissueAccessToken(String accessToken) {
        if (!StringUtils.hasText(accessToken)) return null;

        Token token = tokenService.findByAccessTokenOrThrow(accessToken);
        String refreshToken = token.getRefreshToken();

        if (validateToken(refreshToken)) {
            Authentication authentication = getAuthentication(refreshToken);
            String newAccessToken = generateAccessToken(authentication);
            tokenService.updateToken(newAccessToken, token);
            return newAccessToken;
        }
        return null;
    }

    /** 토큰 유효성 검증 */
    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) return false;
        Claims claims = parseClaims(token);
        return claims.getExpiration().after(new Date());
    }

    /** JWT 파싱 및 예외 처리 */
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
