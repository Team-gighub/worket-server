package gighub.worketserver.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public UserProfileDto getUser(String oauthId) {
        User user = userRepository.findByOauthId(oauthId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        return UserProfileDto.builder()
                .id(user.getId())
                .name(user.getName())
                .provider(user.getProvider().name())
                .role(user.getRole().name())
                .status(user.getStatus().name())
                .phone(user.getPhone())
                .createdAt(user.getCreatedAt().toString())
                .build();
    }

    @Transactional
    public User updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // 필요한 필드만 변경 감지로 업데이트
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        return user;
    }
}

