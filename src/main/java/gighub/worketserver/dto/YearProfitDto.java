package gighub.worketserver.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YearProfitDto {
    private BigDecimal incomes;
    private Integer transactions;  // Integer로 변경
}
