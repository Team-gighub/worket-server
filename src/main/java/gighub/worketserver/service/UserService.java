package gighub.worketserver.service;

import gighub.worketserver.domain.FreelancerProfile;
import gighub.worketserver.domain.User;
import gighub.worketserver.domain.constants.Gender;
import gighub.worketserver.domain.constants.Role;
import gighub.worketserver.domain.constants.Status;
import gighub.worketserver.dto.*;
import gighub.worketserver.global.exception.CommonErrorCode;
import gighub.worketserver.global.exception.RestApiException;
import gighub.worketserver.repository.FreelancerProfileRepository;
import gighub.worketserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 사용자(User) 관련 비즈니스 로직 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
  private final UserRepository userRepository;
  private final FreelancerProfileRepository freelancerProfileRepository;

  /**
   * 모든 사용자 조회 (관리자용)
   */
  public List<User> findAllUsers() {
    return userRepository.findAll();
  }

  /**
   * 사용자 상세 정보 조회
   */
  public UserDetailDto getUserDetail(Long userId) {
    log.info("Getting user detail for user {}", userId);

    // Mock: 사용자 상세 정보
    return UserDetailDto.builder()
      .name("이영은")
      .role(Role.FREELANCER.name())
      .phone("01023970938")
      .birthDate(LocalDate.of(1998, 11, 23))
      .gender(Gender.FEMALE.name())
      .businessSector("디자인")
      .businessSectorYears(8)
      .businessRegistrationNumber("9325863715")
      .build();
  }

  /**
   * 사용자 정보 수정
   */
  @Transactional
  public void updateUser(Long userId, UserUpdateRequest request) {
    log.info("Updating user {}", userId);

    User user = userRepository.findById(userId)
      .orElseThrow(() -> new RuntimeException("User not found"));

    // Mock: 사용자 정보 업데이트
    // TODO: User 엔티티에 업데이트 메서드 추가하여 변경
    log.info("User updated - businessRegistrationNumber: {}", request.getBusinessRegistrationNumber());
  }

  @Transactional
  public void updateUserStatus(Long userId, Status status) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new IllegalArgumentException("User not found"));

    user.updateStatus(status);
  }

  public UserProfileResponse getUserProfile(Long userId) {

    User user = userRepository.findById(userId)
      .orElseThrow(() ->
        new RestApiException(CommonErrorCode.NOT_FOUND, "유저를 찾을 수 없습니다.")
      );

    FreelancerProfile profile = freelancerProfileRepository.findByUserId(userId)
      .orElseThrow(() ->
        new RestApiException(CommonErrorCode.NOT_FOUND, "프로필을 찾을 수 없습니다.")
      );

    return UserProfileResponse.from(user, profile);
  }

  @Transactional
  public UserProfileResponse createOrUpdateProfile(Long userId, UserProfileRequest req) {

    User user = userRepository.findById(userId)
      .orElseThrow(() ->
        new RestApiException(CommonErrorCode.NOT_FOUND, "유저를 찾을 수 없습니다.")
      );

    Optional<FreelancerProfile> optionalProfile =
      freelancerProfileRepository.findByUserId(userId);

    FreelancerProfile profile;

    if (optionalProfile.isEmpty()) {
      // 생성: 프론트 SignUp 단계에서는 업종/업력만 보내기 때문에 이것만 세팅
      profile = FreelancerProfile.builder()
        .user(user)
        .businessSector(req.getBusinessSector())
        .businessSectorYears(req.getBusinessSectorYears())
        .build();

      freelancerProfileRepository.save(profile);

    } else {
      // 갱신
      profile = optionalProfile.get();

      profile.updateProfile(
        req.getBirthDate(),
        req.getGender(),
        req.getBusinessSector(),
        req.getBusinessSectorYears(),
        req.getBusinessRegistrationNumber()
      );
    }

    return UserProfileResponse.from(user, profile);
  }
}
