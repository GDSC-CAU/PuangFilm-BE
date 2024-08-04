package gdsc.cau.puangbe.auth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthPayload {
  private String aud;
  private String sub;
  private String iss;
  private Long exp;
  private String nickname;
}
