package gighub.worketserver.repository;

import gighub.worketserver.domain.OauthToken;
import gighub.worketserver.domain.constants.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OauthTokenRepository extends JpaRepository<OauthToken, Long> {

  Optional<OauthToken> findByUserIdAndProvider(Long userId, Provider provider);
}
