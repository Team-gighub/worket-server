package gighub.worketserver.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusCountDto {
  private String status;
  private Integer count;
}
