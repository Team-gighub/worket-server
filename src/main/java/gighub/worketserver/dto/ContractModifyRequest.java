package gighub.worketserver.dto;

import lombok.*;

/**
 * 계약서 수정 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractModifyRequest {
  private Long transactionId;
  private String userName;
  private String content;
}
