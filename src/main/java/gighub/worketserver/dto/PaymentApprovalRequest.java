package gighub.worketserver.dto;

import gighub.worketserver.domain.constants.ContractType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentApprovalRequest {
  private Long transactionId;
  private String escrowId;
  private String confirmToken;
}
