package gighub.worketserver.service;

import gighub.worketserver.domain.User;
import gighub.worketserver.domain.constants.Status;
import gighub.worketserver.dto.UserProfileDto;
import gighub.worketserver.dto.UserUpdateRequest;
import gighub.worketserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final UserRepository userRepository;

  public List<User> findAllUsers() {
    return userRepository.findAll();
  }

  public UserProfileDto getUser(long userId) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    return UserProfileDto.builder()
      .id(user.getId())
      .name(user.getName())
      .role(user.getRole().name())
      .provider(user.getProvider())
      .status(user.getStatus().name())
      .phone(user.getPhone())
      .createdAt(user.getCreatedAt().toString())
      .build();
  }

  @Transactional
  public User updateUser(Long userId, UserUpdateRequest request) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

    if (request.getName() != null) user.setName(request.getName());
    if (request.getPhone() != null) user.setPhone(request.getPhone());
    return user;
  }

  @Transactional
  public void updateUserStatus(Long userId, Status status) {
    User user = userRepository.findById(userId)
      .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
    user.setStatus(status);
  }
}
