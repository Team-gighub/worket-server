package gighub.worketserver.service;

import gighub.worketserver.domain.User;
import gighub.worketserver.domain.constants.Gender;
import gighub.worketserver.domain.constants.Role;
import gighub.worketserver.dto.UserDetailDto;
import gighub.worketserver.dto.UserProfileDto;
import gighub.worketserver.dto.UserUpdateDto;
import gighub.worketserver.dto.UserUpdateRequest;
import gighub.worketserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 사용자(User) 관련 비즈니스 로직 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
  private final UserRepository userRepository;

  /**
   * 모든 사용자 조회 (관리자용)
   */
  public List<User> findAllUsers() {
    return userRepository.findAll();
  }

  /**
   * 사용자 프로필 조회 (마이페이지)
   */
  public UserProfileDto getUser(Long userId) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new RuntimeException("User not found"));

    return UserProfileDto.builder()
      .id(user.getId())  // userId -> id로 변경
      .name(user.getName())
      .provider(user.getProvider())
      .role(user.getRole().name())
      .status(user.getStatus().name())
      .phone(user.getPhone())
      .createdAt(user.getCreatedAt().toString())
      .build();
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
}
