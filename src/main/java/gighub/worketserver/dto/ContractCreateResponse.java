package gighub.worketserver.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractCreateResponse {
  //거래 ID, 계약 ID 반환
  private Long transactionId;
  private Long contractId;
}
