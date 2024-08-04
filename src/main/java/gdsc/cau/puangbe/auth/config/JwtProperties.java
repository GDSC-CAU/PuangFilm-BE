package gdsc.cau.puangbe.auth.config;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@ConfigurationProperties( prefix = "jwt")
@RequiredArgsConstructor
public class JwtProperties {
  private final String secretKey;
  private final Long accessTokenValidityInSeconds;
  private final Long refreshTokenValidityInSeconds;
}
