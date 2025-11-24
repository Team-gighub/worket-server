package gighub.worketserver.service;

import gighub.worketserver.domain.Transaction;
import gighub.worketserver.dto.MonthlyStatisticsDto;
import gighub.worketserver.dto.StatisticsResponse;
import gighub.worketserver.dto.YearProfitDto;
import gighub.worketserver.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 통계 관련 비즈니스 로직 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StatisticsService {

  private final TransactionRepository transactionRepository;

  /**
   * 통계 조회 (소득관리 탭)
   */
  public StatisticsResponse getStatistics(Authentication authentication) {
    Long userId = Long.parseLong(authentication.getName());
    log.info("Getting statistics for user {}", userId);
    //1. 3달을 받아올거니까 현재기준-2 구함
    LocalDate threeMonthsAgo = LocalDate.now().minusMonths(2);
    LocalDateTime settledAtStart = threeMonthsAgo
      .with(TemporalAdjusters.firstDayOfMonth())
      .atStartOfDay();
    Sort sort = Sort.by(Sort.Direction.ASC, "settledAt");
    //2. 정산일이 현재기준~3달까지 가져옴
    List<Transaction> threeMonthTransactions = transactionRepository.findByContract_FreelancerIdAndSettledAtGreaterThanEqual(userId, settledAtStart, sort);

    Map<String, List<Transaction>> transactionsByMonth = threeMonthTransactions.stream()
      .collect(Collectors.groupingBy(
        transaction -> transaction.getSettledAt().format(DateTimeFormatter.ofPattern("yyyy-MM"))
      ));

    List<MonthlyStatisticsDto> statistics = transactionsByMonth.entrySet().stream()
      .map(entry -> {
        String month = entry.getKey();
        List<Transaction> monthTransactions = entry.getValue();

        // 해당 월의 총 소득액 합계 계산
        BigDecimal totalIncome = monthTransactions.stream()
          .map(Transaction::getAmount)
          .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 해당 월의 거래 건수 계산
        int transactionCount = monthTransactions.size();

        return MonthlyStatisticsDto.builder()
          .month(month)
          .incomes(totalIncome)
          .transactions(transactionCount)
          .build();
      })
      .sorted(Comparator.comparing(MonthlyStatisticsDto::getMonth))//'달' 기준 정렬이 필요
      .collect(Collectors.toList());

    //연간 소득,거래수 게산 로직
    //1. 올해의 년도 구하기
    LocalDate startOfYear = LocalDate.now().with(TemporalAdjusters.firstDayOfYear());
    LocalDateTime thisYear = startOfYear.atStartOfDay();
    //2. 올해의 거래 내역 가져오기
    List<Transaction> currentYearTransactions =
      transactionRepository.findByContract_FreelancerIdAndSettledAtGreaterThanEqual(userId, settledAtStart, sort);

    //3. 올해의 소득액 합산
    BigDecimal totalYearIncome = currentYearTransactions.stream()
      .map(Transaction::getAmount)
      .reduce(BigDecimal.ZERO, BigDecimal::add);
    //4. 올해의 거래수 계산
    int totalYearTransactions = currentYearTransactions.size();

    List<YearProfitDto> currentYearProfit = new ArrayList<>();
    currentYearProfit.add(YearProfitDto.builder()
      .incomes(totalYearIncome)
      .transactions(totalYearTransactions)
      .build());

    return StatisticsResponse.builder()
      .currentYearProfit(currentYearProfit)
      .statistics(statistics)
      .build();
  }
}
