package gighub.worketserver.user;

import gighub.worketserver.user.constants.Provider;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByOauthIdAndProvider(String oauthId, Provider provider);

    Optional<User> findByOauthId(String oauthId);
}
