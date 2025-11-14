package gighub.worketserver.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractExtractResponse {
    private ContractInfoDto contractInfo;
    private ClientInfoDto clientInfo;
    private FreelancerInfoDto freelancerInfo;
}
