package gighub.worketserver.repository;

import gighub.worketserver.domain.ContractFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContractFileRepository extends JpaRepository<ContractFile, Long> {
  @Override
  Optional<ContractFile> findById(Long contractId);


}
