package gighub.worketserver.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MonthlyStatisticsDto {
    private String month;
    private BigDecimal incomes;  // BigDecimal로 변경
    private Integer transactions;
}
