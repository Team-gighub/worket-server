package gighub.worketserver.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoreApprovalRequest {
  private String escrowId;
  private String confirmToken;
}
