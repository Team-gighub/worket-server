package gighub.worketserver.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionDetailResponse {
  private String status;
  private String signedAt;
  private String depositHoldAt;
  private String paymentConfirmedAt;
  private String settledAt;
  private String createdAt;
  private Long contractId;
  private BigDecimal settledAmount;
  private String contractFileUrl;
  private ContractInfoDto contractInfo;
  private ClientInfoDto clientInfo;
  private FreelancerInfoDto freelancerInfo;
}
