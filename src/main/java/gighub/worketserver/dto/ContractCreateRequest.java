package gighub.worketserver.dto;

import gighub.worketserver.domain.constants.ContractType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractCreateRequest {
    private ContractType type;
    private ContractInfoDto contractInfo;
    private ClientInfoDto clientInfo;
    private FreelancerInfoDto freelancerInfo;
}
