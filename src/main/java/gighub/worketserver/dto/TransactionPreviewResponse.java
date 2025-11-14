package gighub.worketserver.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionPreviewResponse {
  private String freelancerName;
  private String clientName;
  private String title;
}
