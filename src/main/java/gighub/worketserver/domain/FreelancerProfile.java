package gighub.worketserver.domain;

import gighub.worketserver.domain.constants.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "freelancer_profile")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FreelancerProfile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "freelancer_profile_id")
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Column(name = "birth_date")
  private LocalDate birthDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "gender", length = 10)
  private Gender gender;

  @Column(name = "business_sector", length = 100)
  private String businessSector;

  @Column(name = "business_sector_years")
  private Long businessSectorYears;

  @Column(name = "business_registration_number", length = 20)
  private String businessRegistrationNumber;

  public void updateProfile(
    LocalDate birthDate,
    Gender gender,
    String businessSector,
    Long businessSectorYears,
    String businessRegistrationNumber
  ) {
    if (birthDate != null) {
      this.birthDate = birthDate;
    }
    if (gender != null) {
      this.gender = gender;
    }
    if (businessSector != null) {
      this.businessSector = businessSector;
    }
    if (businessSectorYears != null) {
      this.businessSectorYears = businessSectorYears;
    }
    if (businessRegistrationNumber != null) {
      this.businessRegistrationNumber = businessRegistrationNumber;
    }
  }
}
