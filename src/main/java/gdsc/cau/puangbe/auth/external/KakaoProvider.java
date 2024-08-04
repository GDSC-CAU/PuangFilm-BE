package gdsc.cau.puangbe.auth.external;

import gdsc.cau.puangbe.auth.config.KakaoLoginProperties;
import gdsc.cau.puangbe.auth.dto.KakaoIdTokenPublicKey;
import gdsc.cau.puangbe.auth.dto.OAuthTokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
@CacheConfig(cacheNames = "kakaoPublicKeyList")
public class KakaoProvider {
  private final KakaoLoginProperties kakaoLoginProperties;

  public OAuthTokenResponse getTokenByCode(String code) {
    return WebClient.create()
        .post()
        .uri(kakaoLoginProperties.getTokenUri())
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        .body(BodyInserters.
            fromFormData("grant_type", "authorization_code")
            .with("client_id", kakaoLoginProperties.getClientId())
            .with("redirect_uri", kakaoLoginProperties.getRedirectUri())
            .with("code", code)
            .with("client_secret", kakaoLoginProperties.getClientSecret()))
        .retrieve()
        .bodyToMono(OAuthTokenResponse.class)
        .block();
  }

  @Cacheable(key = "'all'")
  public KakaoIdTokenPublicKey getOIDCPublicKeyList() {
    return WebClient.create()
        .get()
        .uri(kakaoLoginProperties.getPublicKeyUri())
        .retrieve()
        .bodyToMono(KakaoIdTokenPublicKey.class)
        .block();
  }

  public KakaoIdTokenPublicKey getUpdatedOIDCPublicKeyList() {
    return getOIDCPublicKeyList();
  }

  // 매일 새벽 5시에 실행
  @Scheduled(cron = "0 0 5 * * ?") // 초 분 시 일 월 요일 (연도)
  public void updateKakaoPublicKeyListCache() {
    getOIDCPublicKeyList();
  }
}
