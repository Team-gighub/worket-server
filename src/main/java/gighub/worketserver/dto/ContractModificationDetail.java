package gighub.worketserver.dto;

import lombok.*;

/**
 * 수정 계약서 단건 조회 DTO
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractModificationDetail {
  private FreelancerInfoDto freelancerInfoDto;
  private ClientInfoDto clientInfoDto;
  private ContractInfoDto contractInfoDto;
  private String content;
  private long contractId;
}
