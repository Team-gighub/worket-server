package gighub.worketserver.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PasscodeRegisterRequest {
  private String pin;
}
