package gighub.worketserver.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticsResponse {
  private YearProfitDto currentYearProfit;
  private List<MonthlyStatisticsDto> statistics;
}
