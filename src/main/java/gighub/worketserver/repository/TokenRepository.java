package gighub.worketserver.repository;

import gighub.worketserver.domain.Token;
import org.springframework.data.jpa.repository.JpaRepository;


public interface TokenRepository extends JpaRepository<Token, String> {
}
