package gighub.worketserver.repository;

import gighub.worketserver.domain.Contract;
import gighub.worketserver.domain.Transaction;
import gighub.worketserver.domain.constants.TransactionStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
  Transaction findByContract(Contract contract);

  List<Transaction> findBySettledAtGreaterThanEqual(LocalDateTime settledAtStart, Sort sort);
}
