package gighub.worketserver.repository;

import gighub.worketserver.domain.ContractModify;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractModifyRepository extends JpaRepository<ContractModify, Long> {
}
