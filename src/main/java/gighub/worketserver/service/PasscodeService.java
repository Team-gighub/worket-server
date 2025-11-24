package gighub.worketserver.service;

import gighub.worketserver.domain.User;
import gighub.worketserver.global.exception.*;
import gighub.worketserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PasscodeService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 패스코드 등록
    @Transactional
    public void registerPasscode(Long userId, String rawPasscode) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(CommonErrorCode.NOT_FOUND, "유저를 찾을 수 없습니다."));

        // passcode 암호화 필수
        String encoded = passwordEncoder.encode(rawPasscode);
        user.updatePasscode(encoded);
    }

    // 패스코드 검증
    public void verifyPasscode(Long userId, String rawPasscode) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RestApiException(CommonErrorCode.NOT_FOUND, "유저를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(rawPasscode, user.getPasscode())) {
            throw new PasscodeException(PasscodeErrorCode.INVALID_PASSCODE);
        }
    }
}
