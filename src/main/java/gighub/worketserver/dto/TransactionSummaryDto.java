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
  private String startDate;
  private String endDate;
}
