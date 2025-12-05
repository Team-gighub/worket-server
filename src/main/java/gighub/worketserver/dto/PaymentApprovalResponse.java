package gighub.worketserver.dto;

import gighub.worketserver.domain.constants.ContractType;
import gighub.worketserver.domain.constants.HoldStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentApprovalResponse {
    private String escrowId;
    private BigDecimal holdAmount;
    private HoldStatus holdStatus;
    private BigDecimal platformFee;
    private LocalDateTime holdStartDatetime;
  }
