package gighub.worketserver.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FreelancerInfoDto {
    private String name;
    private String phone;
    private String account;
    private String bank;
}
