package gdsc.cau.puangbe.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Token {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "refresh_token_id")
  private Long id;

  private String refreshToken;

  private String kakaoId;

  private LocalDateTime expiresAt;

  @Builder
  public Token(String refreshToken, String kakaoId, LocalDateTime expiresAt){
    this.refreshToken = refreshToken;
    this.kakaoId = kakaoId;
    this.expiresAt = expiresAt;
  }

  public void update(String refreshToken, LocalDateTime expiresAt) {
    this.refreshToken = refreshToken;
    this.expiresAt = expiresAt;
  }
}
