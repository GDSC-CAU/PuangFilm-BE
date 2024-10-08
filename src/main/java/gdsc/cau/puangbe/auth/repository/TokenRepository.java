package gdsc.cau.puangbe.auth.repository;

import gdsc.cau.puangbe.auth.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByKakaoId(String kakaoId);
    @Transactional
    void deleteAllByExpiresAtBefore(LocalDateTime expiresAt);
}
