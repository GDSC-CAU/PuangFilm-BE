package gdsc.cau.puangbe.user.repository;

import gdsc.cau.puangbe.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByKakaoId(String kakaoId);
}
