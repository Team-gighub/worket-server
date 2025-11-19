package gighub.worketserver.global.security.token;

import gighub.worketserver.domain.User;
import gighub.worketserver.global.exception.TokenException;
import gighub.worketserver.global.security.dto.PrincipalDetails;
import gighub.worketserver.repository.UserRepository;
import gighub.worketserver.service.RefreshTokenService;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import java.time.ZoneId;

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
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static gighub.worketserver.global.exception.TokenErrorCode.*;

@Slf4j
@RequiredArgsConstructor
@Component
public class TokenProvider {

    private final RefreshTokenService refreshTokenService;
    private final UserRepository userRepository;

    @Value("${JWT_SECRET_KEY}")
    private String key;

    private SecretKey secretKey;


    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000L * 60 * 30; // 30분
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 7; // 7일
    private static final String KEY_ROLE = "role";

    @PostConstruct
    private void initKey() {
        secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(key));
    }

    /**
     * AccessToken 발급
     */
    public String generateAccessToken(Authentication authentication) {
        return generateToken(authentication, ACCESS_TOKEN_EXPIRE_TIME);
    }

    /**
     * RefreshToken 발급 및 DB 저장
     */
    @Transactional
    public String generateRefreshToken(Authentication authentication) {
        String refreshToken = generateToken(authentication, REFRESH_TOKEN_EXPIRE_TIME);

        Claims claims = parseClaims(refreshToken);
        LocalDateTime expiresAt = claims.getExpiration()
                .toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        Long userId = Long.parseLong(authentication.getName());
        refreshTokenService.saveRefreshToken(userId, refreshToken, expiresAt);
        return refreshToken;
    }

    /**
     * JWT 생성 공통 로직
     */
    private String generateToken(Authentication authentication, long expireTime) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expireTime);

        Object principalObj = authentication.getPrincipal();
        String userId;

        // PrincipalDetails 하나만 인정
        if (principalObj instanceof PrincipalDetails details) {
            userId = String.valueOf(details.getUser().getId());
        } else {
            throw new IllegalStateException(
                    "지원되지 않는 Principal 타입: " + principalObj.getClass()
            );
        }

        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        return Jwts.builder()
                .subject(userId)
                .claim(KEY_ROLE, authorities)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey, Jwts.SIG.HS512)
                .compact();
    }

    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        Long userId = Long.parseLong(claims.getSubject());

        // DB에서 진짜 User 엔티티 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new TokenException(INVALID_TOKEN));

        PrincipalDetails principal = new PrincipalDetails(
                user,
                Collections.emptyMap() // attributes는 OAuth 로그인 시만 필요
        );

        Collection<SimpleGrantedAuthority> authorities =
                Collections.singletonList(new SimpleGrantedAuthority(user.getRole().name()));

        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }


    /**
     * Refres
     * <p>
     * /** RefreshToken으로 AccessToken 재발급
     */
    @Transactional
    public String reissueAccessToken(String refreshToken) {
        if (!StringUtils.hasText(refreshToken)) return null;
        if (!validateToken(refreshToken)) return null;

        Authentication authentication = getAuthentication(refreshToken);
        Long userId = Long.parseLong(authentication.getName());

        String storedRefreshToken = refreshTokenService.findRefreshTokenOrThrow(userId);
        if (!refreshToken.equals(storedRefreshToken)) {
            throw new TokenException(INVALID_TOKEN);
        }

        return generateAccessToken(authentication);
    }

    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        if (!StringUtils.hasText(token)) return false;
        try {
            Claims claims = parseClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (ExpiredJwtException e) {
            log.warn("토큰 검증 실패: {}", e.getMessage()); // log는 나중에 지울 예정
            throw new TokenException(EXPIRED_TOKEN);

        } catch (UnsupportedJwtException | MalformedJwtException | SecurityException | IllegalArgumentException e) {
            // 서명 위조, 포맷 깨짐, 지원하지 않는 형식 등
            throw new TokenException(INVALID_TOKEN); // 또는 INVALID_JWT_SIGNATURE

        } catch (Exception e) {
            log.warn("토큰 검증 실패: {}", e.getMessage());
            return false;
        }
    }

    /**
     * JWT 파싱
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Transactional
    public String reissueRefreshToken(String oldRefreshToken) {
        if (!validateToken(oldRefreshToken)) {
            throw new TokenException(INVALID_TOKEN);
        }

        Authentication authentication = getAuthentication(oldRefreshToken);
        String newRefreshToken = generateToken(authentication, REFRESH_TOKEN_EXPIRE_TIME);

        Long userId = Long.parseLong(authentication.getName());
        LocalDateTime expiresAt = LocalDateTime.now()
                .plusSeconds(REFRESH_TOKEN_EXPIRE_TIME / 1000); // 밀리초 단위 → 초 단위 변환

        refreshTokenService.saveRefreshToken(userId, newRefreshToken, expiresAt);

        return newRefreshToken;
    }
}
