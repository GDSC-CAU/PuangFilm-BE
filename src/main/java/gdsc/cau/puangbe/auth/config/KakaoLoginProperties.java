package gdsc.cau.puangbe.auth.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties( prefix = "oauth2.kakao")
@RequiredArgsConstructor
public class KakaoLoginProperties {
  private final String clientId;
  private final String clientSecret;
  private final String redirectUri;
  private final String tokenUri;
  private final String metadataUri;
  private final String publicKeyUri;
}
