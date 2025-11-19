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

  @EntityGraph(attributePaths = {
    "contract", // 1단계: Transaction -> Contract 로딩
    "contract.freelancer", // 2단계: Contract -> Freelancer(User) 로딩
    "contract.client" // 2단계: Contract -> Client(User) 로딩
  })
    // 💡 JpaRepository의 기본 메서드를 오버라이드하여 EntityGraph를 적용합니다.
  Optional<Transaction> findById(Long id);

  @Query("""
    SELECT t FROM Transaction t
    JOIN FETCH t.contract c
    JOIN FETCH c.client cl
    JOIN FETCH c.freelancer fr
    WHERE t.id = :transactionId
      AND (cl.id = :userId OR fr.id = :userId)
  """)
  Optional<Transaction> findByIdAndUserId(Long transactionId, Long userId);

}
