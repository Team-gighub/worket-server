package gighub.worketserver.repository;

import gighub.worketserver.domain.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  /**
   * Transaction 존재 여부 확인
   * /transactions/{transactionId} API용
   */
  @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
    "FROM Transaction t WHERE t.id = :transactionId")
  boolean existsByTransactionId(@Param("transactionId") Long transactionId);

  /**
   * Transaction에 대한 사용자 권한 확인
   * /transactions/{transactionId} API용
   * <p>
   * 프리랜서 또는 의뢰인인 경우 true 반환
   */
  @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
    "FROM Transaction t " +
    "JOIN t.contract c " +
    "WHERE t.id = :transactionId " +
    "AND (c.freelancer.id = :userId OR c.client.id = :userId)")
  boolean hasPermission(
    @Param("transactionId") Long transactionId,
    @Param("userId") Long userId
  );

  /**
   * Transaction ID로 상세 조회 (Contract, User 정보 포함)
   * /transactions/{transactionId} API용 - 3단계
   */
  @Query("SELECT t FROM Transaction t " +
    "JOIN FETCH t.contract c " +
    "JOIN FETCH c.freelancer f " +
    "JOIN FETCH c.client cl " +
    "WHERE t.id = :transactionId")
  Optional<Transaction> findByIdWithDetails(@Param("transactionId") Long transactionId);
}
