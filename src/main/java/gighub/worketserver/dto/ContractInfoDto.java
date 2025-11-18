package gighub.worketserver.dto;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContractInfoDto {
  private String title;
  private BigDecimal amount;
  private String startDate;
  private String endDate;
}
