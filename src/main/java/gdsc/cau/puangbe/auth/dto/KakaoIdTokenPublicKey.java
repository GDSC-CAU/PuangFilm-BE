package gdsc.cau.puangbe.auth.dto;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class KakaoIdTokenPublicKey {
  private List<KakaoJWK> keys;
}
