package gighub.worketserver.dto;

import gighub.worketserver.domain.FreelancerProfile;
import gighub.worketserver.domain.User;
import gighub.worketserver.domain.constants.Role;
import gighub.worketserver.domain.constants.Gender;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class UserProfileResponse {
  private String name;
  private Role role;
  private String phone;
  private LocalDate birthDate;
  private Gender gender;
  private String businessSector;
  private Long businessSectorYears;
  private String businessRegistrationNumber;

  public static UserProfileResponse from(User user, FreelancerProfile profile) {
    return UserProfileResponse.builder()
      .name(user.getName())
      .role(user.getRole())
      .phone(user.getPhone())
      .birthDate(profile.getBirthDate())
      .gender(profile.getGender())
      .businessSector(profile.getBusinessSector())
      .businessSectorYears(profile.getBusinessSectorYears())
      .businessRegistrationNumber(profile.getBusinessRegistrationNumber())
      .build();
  }
}
