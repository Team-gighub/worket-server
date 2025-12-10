package gighub.worketserver.dto;

import gighub.worketserver.domain.constants.HoldStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreApprovalResponse {
    private String escrowId;
    private BigDecimal holdAmount;
    private HoldStatus holdStatus;
    private BigDecimal platformFee;
    private LocalDateTime holdStartDatetime;
    private String payerBankCode;
    private String payerAccount;
  }
