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
    private String signedAt;           // LocalDateTime -> String으로 되돌림
    private String depositHoldAt;      // LocalDateTime -> String으로 되돌림
    private String paymentConfirmedAt; // LocalDateTime -> String으로 되돌림
    private String settledAt;          // LocalDateTime -> String으로 되돌림
    private String createdAt;          // LocalDateTime -> String으로 되돌림
    private Long contractId;
    private BigDecimal settledAmount;
    private String contractFileUrl;
    private ContractInfoDto contractInfo;
    private ClientInfoDto clientInfo;
    private FreelancerInfoDto freelancerInfo;
}
