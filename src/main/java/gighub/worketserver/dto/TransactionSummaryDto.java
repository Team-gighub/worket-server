package gighub.worketserver.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionSummaryDto {
    private Long transactionId;
    private String title;
    private String status;
    private BigDecimal amount;
    private String startDate;  // LocalDate -> String으로 되돌림
    private String endDate;    // LocalDate -> String으로 되돌림
}
