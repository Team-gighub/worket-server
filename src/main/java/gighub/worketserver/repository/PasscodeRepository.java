package gighub.worketserver.repository;

import gighub.worketserver.domain.Passcode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasscodeRepository extends JpaRepository<Passcode, Long> {
  Optional<Passcode> findByUserId(Long userId);
}
