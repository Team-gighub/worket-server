package gighub.worketserver.dto;

import gighub.worketserver.domain.constants.Gender;
import lombok.Getter;

import java.time.LocalDate;

@Getter
public class UserProfileRequest {

  private String businessSector;
  private Long businessSectorYears;

  private LocalDate birthDate;
  private Gender gender;
  private String businessRegistrationNumber;
}

