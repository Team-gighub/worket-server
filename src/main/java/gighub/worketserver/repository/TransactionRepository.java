package gighub.worketserver.repository;

import gighub.worketserver.domain.Transaction;
import gighub.worketserver.domain.constants.TransactionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  /**
   * Transaction ID로 상세 조회 (Contract, Client, Freelancer 포함)
   */
  @Query("""
        SELECT t FROM Transaction t
        LEFT JOIN FETCH t.contract c
        LEFT JOIN FETCH c.client
        LEFT JOIN FETCH c.freelancer
        WHERE t.id = :id
    """)
  Optional<Transaction> findByIdWithContractAndUsers(@Param("id") Long id);

  /**
   * Transaction 존재 여부 확인
   * /transactions/{transactionId} API용
   */
  @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END " +
    "FROM Transaction t WHERE t.id = :transactionId")
  boolean existsByTransactionId(@Param("transactionId") Long transactionId);

  /**
   * Transaction에 대한 사용자 권한 확인
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
   * 프리랜서의 년/월별 거래 목록 조회
   */
  @Query("SELECT t FROM Transaction t " +
    "JOIN FETCH t.contract c " +
    "WHERE c.freelancer.id = :freelancerId " +
    "AND YEAR(t.createdAt) = :year " +
    "AND MONTH(t.createdAt) = :month")
  List<Transaction> findByFreelancerIdAndYearMonth(
    @Param("freelancerId") Long freelancerId,
    @Param("year") int year,
    @Param("month") int month
  );

  /**
   * 프리랜서의 특정 상태 거래 건수 조회
   */
  @Query("SELECT COUNT(t) FROM Transaction t " +
    "JOIN t.contract c " +
    "WHERE c.freelancer.id = :freelancerId " +
    "AND t.status = :status")
  Long countByFreelancerIdAndStatus(
    @Param("freelancerId") Long freelancerId,
    @Param("status") TransactionStatus status
  );
}
