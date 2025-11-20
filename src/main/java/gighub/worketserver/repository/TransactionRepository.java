package gighub.worketserver.repository;

import gighub.worketserver.domain.Transaction;
import gighub.worketserver.domain.constants.TransactionStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

//  List<Transaction> findByFreelancerId(Long freelancerId);

//  @Query("SELECT t FROM Transaction t WHERE t.freelancer.id = :freelancerId " +
//    "AND YEAR(t.createdAt) = :year AND MONTH(t.createdAt) = :month")
//  List<Transaction> findByFreelancerIdAndYearMonth(
//    @Param("freelancerId") Long freelancerId,
//    @Param("year") int year,
//    @Param("month") int month
//  );
//
//  @Query("SELECT COUNT(t) FROM Transaction t WHERE t.freelancer.id = :freelancerId " +
//    "AND t.status = :status")
//  Long countByFreelancerIdAndStatus(
//    @Param("freelancerId") Long freelancerId,
//    @Param("status") TransactionStatus status
//  );

  /**
   * 기본 findById 오버라이드 (EntityGraph 적용)
   * 이렇게 하면 코드 변경 없이 자동으로 EntityGraph 적용
   */

  @Query("""
    SELECT t FROM Transaction t
    LEFT JOIN FETCH t.contract c
    LEFT JOIN FETCH c.client
    LEFT JOIN FETCH c.freelancer
    WHERE t.id = :id
""")
  Optional<Transaction> findByIdWithContractAndUsers(@Param("id") Long id);



}
