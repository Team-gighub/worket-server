package gighub.worketserver.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 수정 계약서 조회 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractModificationResponse {
  private Long modificationId;
  private Long transactionId;
  private String userName;
  private String status;
  private LocalDateTime createdAt;
}
