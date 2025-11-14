package gighub.worketserver.service;

import gighub.worketserver.dto.MonthlyStatisticsDto;
import gighub.worketserver.dto.StatisticsResponse;
import gighub.worketserver.dto.YearProfitDto;
import gighub.worketserver.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
        
        // Mock: 올해 총 수익 및 거래 수
        List<YearProfitDto> currentYearProfit = new ArrayList<>();
        currentYearProfit.add(YearProfitDto.builder()
                .incomes(BigDecimal.valueOf(68500000))
                .transactions(95)
                .build());
        
        // Mock: 최근 3개월 통계
        List<MonthlyStatisticsDto> statistics = new ArrayList<>();
        LocalDate now = LocalDate.now();
        
        for (int i = 2; i >= 0; i--) {
            LocalDate month = now.minusMonths(i);
            statistics.add(MonthlyStatisticsDto.builder()
                    .month(String.format("%d-%02d", month.getYear(), month.getMonthValue()))
                    .incomes(BigDecimal.valueOf((i + 1) * 3000000L))
                    .transactions((i + 1) * 5)
                    .build());
        }
        
        return StatisticsResponse.builder()
                .currentYearProfit(currentYearProfit)
                .statistics(statistics)
                .build();
    }
}
