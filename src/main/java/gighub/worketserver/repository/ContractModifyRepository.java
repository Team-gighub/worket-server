package gighub.worketserver.repository;

import gighub.worketserver.domain.ContractModify;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContractModifyRepository extends JpaRepository<ContractModify, Long> {

  /**
   * 모든 계약서 수정 요청 목록을 조회하며, 연관된 Transaction 엔티티 로딩
   *
   * @return ContractModify 엔티티와 그에 연결된 Transaction이 포함된 리스트
   */
  @Query("SELECT cm FROM ContractModify cm JOIN FETCH cm.transaction")
  List<ContractModify> findAllWithTransaction();

  /**
   * 특정 ID를 가진 계약서 수정 요청
   * 연관된 Transaction과 Contract 엔티티 함께 로딩
   *
   * @param id 조회할 ContractModify ID
   * @return Transaction 및 Contract가 FETCH된 ContractModify 객체 (Optional)
   */
  @Query("SELECT cm FROM ContractModify cm " +
    "JOIN FETCH cm.transaction t " + // Transaction을 로딩
    "JOIN FETCH t.contract " +      // Transaction을 통해 Contract까지 로딩
    "WHERE cm.id = :id")
  Optional<ContractModify> findWithTransactionAndContractById(@Param("id") Long id);

}
