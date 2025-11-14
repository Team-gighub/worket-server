package gighub.worketserver.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractInfoDto {
    private String title;
    private BigDecimal amount;
    private String startDate;  // LocalDate -> String으로 되돌림
    private String endDate;    // LocalDate -> String으로 되돌림
}
