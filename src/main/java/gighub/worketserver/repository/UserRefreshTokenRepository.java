package gighub.worketserver.repository;

import gighub.worketserver.domain.UserRefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRefreshTokenRepository extends JpaRepository<UserRefreshToken, Long> {

  Optional<UserRefreshToken> findByRefreshTokenAndIsRevokedFalse(String refreshToken);

  Optional<UserRefreshToken> findByUserIdAndIsRevokedFalse(Long userId);
}
