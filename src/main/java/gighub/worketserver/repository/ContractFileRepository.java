package gighub.worketserver.repository;

import gighub.worketserver.domain.ContractFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ContractFileRepository extends JpaRepository<ContractFile, Long> {
}
