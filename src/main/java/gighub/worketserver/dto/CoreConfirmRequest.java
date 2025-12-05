package gighub.worketserver.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreConfirmRequest {
  private String merchantId;
  private String escrowId;
}
