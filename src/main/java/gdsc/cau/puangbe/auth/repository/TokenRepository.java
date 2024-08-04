package gdsc.cau.puangbe.auth.repository;

import gdsc.cau.puangbe.auth.entity.Token;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

public interface TokenRepository extends JpaRepository<Token, Long> {
  Optional<Token> findByKakaoId(String kakaoId);
}
