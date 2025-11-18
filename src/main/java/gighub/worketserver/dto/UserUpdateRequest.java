package gighub.worketserver.dto;

import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserUpdateRequest {
  private LocalDate birthDate;
  private String gender;
  private String businessSector;
  private Integer businessSectorYears;
  private String businessRegistrationNumber;
}
