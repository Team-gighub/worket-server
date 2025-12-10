package gighub.worketserver.service;

import gighub.worketserver.domain.OauthToken;
import gighub.worketserver.domain.constants.Provider;
import gighub.worketserver.repository.OauthTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoTokenRefreshService {

  private final OauthTokenRepository oauthTokenRepository;
  private final RestTemplate restTemplate = new RestTemplate();

  @Value("${spring.security.oauth2.client.registration.kakao.client-id}")
  private String clientId;

  @Value("${spring.security.oauth2.client.registration.kakao.client-secret:}")
  private String clientSecret;

  /**
   * 카카오 OAuth Access Token 재발급
   */
  public String refreshKakaoAccessToken(Long userId, OauthToken oauthToken) {
    String refreshToken = oauthToken.getRefreshToken();

    if (refreshToken == null) {
      log.error("카카오 Refresh Token이 없음 - UserId: {}", userId);
      throw new IllegalStateException("카카오 로그인이 필요합니다. 다시 로그인해주세요.");
    }

    try {
      // 카카오 토큰 갱신 API 호출
      Map<String, Object> tokenResponse = requestKakaoTokenRefresh(refreshToken);

      String newAccessToken = (String) tokenResponse.get("access_token");

      // 응답에서 expires_in 추출 (초 단위, 보통 21600 = 6시간)
      Integer expiresIn = (Integer) tokenResponse.get("expires_in");
      LocalDateTime expiresAt = expiresIn != null
        ? LocalDateTime.now().plusSeconds(expiresIn)
        : LocalDateTime.now().plusHours(6);

      // DB 업데이트
      oauthToken.updateAccessToken(newAccessToken);

      // 새로 리프레쉬 토큰을 발급해주는 거 처음 발급 받으면 만료기간이 6달 정도 되는데
      // 이걸 확인해볼수가 없음 카카오는 1달정도 남았을때만 리프레쉬 토큰을 재발급해준다.
      String newRefreshToken = (String) tokenResponse.get("refresh_token");
      if (newRefreshToken != null) {
        log.info("카카오 Refresh Token도 갱신됨 - UserId: {}", userId);

        Integer refreshExpiresIn = (Integer) tokenResponse.get("refresh_token_expires_in");
        LocalDateTime refreshExpiresAt = refreshExpiresIn != null
          ? LocalDateTime.now().plusSeconds(refreshExpiresIn)
          : null;

        oauthToken.updateRefreshToken(newRefreshToken);
        oauthToken.updateRefreshTokenExpiresAt(refreshExpiresAt);
      }

      oauthTokenRepository.save(oauthToken);

      log.info("카카오 Access Token 재발급 완료 - UserId: {}", userId);
      return newAccessToken;

    } catch (HttpClientErrorException.Unauthorized e) {
      // 401 에러: Refresh Token도 만료됨
      log.error("카카오 Refresh Token 만료 - UserId: {}", userId);

      // DB에서 토큰 정보 삭제 (재로그인 시 새로 받을 것)
      oauthTokenRepository.delete(oauthToken);

      throw new IllegalStateException("카카오 로그인이 만료되었습니다. 다시 로그인해주세요.");

    } catch (Exception e) {
      log.error("카카오 토큰 재발급 실패 - UserId: {}, Error: {}", userId, e.getMessage(), e);
      throw new IllegalStateException("카카오 토큰 갱신에 실패했습니다. 다시 로그인해주세요.");
    }
  }

  /**
   * 카카오 토큰 갱신 API 호출
   */
  private Map<String, Object> requestKakaoTokenRefresh(String refreshToken) {
    String url = "https://kauth.kakao.com/oauth/token";

    MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
    params.add("grant_type", "refresh_token");
    params.add("client_id", clientId);
    params.add("refresh_token", refreshToken);

    if (clientSecret != null && !clientSecret.isEmpty()) {
      params.add("client_secret", clientSecret);
    }

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

    HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

    // 카카오 API 호출 (예외는 상위로 전파)
    ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

    if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
      return response.getBody();
    }

    throw new RuntimeException("카카오 토큰 갱신 실패");
  }

  @Transactional
  public String refreshKakaoAccessToken(Long userId) {
    OauthToken oauthToken = oauthTokenRepository.findByUserIdAndProvider(userId, Provider.KAKAO)
      .orElseThrow(() -> {
        log.error("카카오 OAuth 토큰이 없습니다. userId = {}", userId);
        throw new IllegalStateException("카카오 로그인이 필요합니다. 다시 로그인해주세요.");
      });

    return refreshKakaoAccessToken(userId, oauthToken);
  }
}
