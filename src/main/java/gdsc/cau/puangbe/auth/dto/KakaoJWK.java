package gdsc.cau.puangbe.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KakaoJWK {
  private String kid;

  private String kty;

  private String alg;

  private String use;

  private String n;

  private String e;
}
