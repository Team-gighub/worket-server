package gighub.worketserver.service;

import gighub.worketserver.domain.Passcode;
import gighub.worketserver.dto.PasscodeRegisterRequest;
import gighub.worketserver.dto.PasscodeVerifyRequest;
import gighub.worketserver.repository.PasscodeRepository;
import gighub.worketserver.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 비즈니스 로직 Service
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasscodeRepository passcodeRepository;

    /**
     * 로그아웃
     */
    @Transactional
    public void logout(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        String userId = authentication.getName();
        log.info("User {} logging out", userId);
        
        // Mock: 토큰 폐기 로직
        // TODO: RefreshToken 삭제, 쿠키 삭제
    }

    /**
     * 카카오 토큰 재발급
     */
    @Transactional
    public void refreshKakaoToken(Authentication authentication) {
        String userId = authentication.getName();
        log.info("Refreshing Kakao token for user {}", userId);
        
        // Mock: 카카오 토큰 재발급 로직
        // TODO: 카카오 API 호출하여 토큰 갱신
    }

    /**
     * 회원 탈퇴
     */
    @Transactional
    public void unlinkUser(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        String userId = authentication.getName();
        log.info("Unlinking user {}", userId);
        
        // Mock: 회원 탈퇴 로직
        // TODO: 카카오 연동 해제, 사용자 상태 변경, 토큰 폐기
    }

    /**
     * 간편 비밀번호 등록
     */
    @Transactional
    public void registerPasscode(Authentication authentication, PasscodeRegisterRequest request) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("Registering passcode for user {}", userId);
        
        // Mock: 간편 비밀번호 저장
        Passcode passcode = Passcode.builder()
                .userId(userId)
                .pin(request.getPin()) // 실제로는 해시값을 저장
                .build();
        
        passcodeRepository.save(passcode);
    }

    /**
     * 간편 비밀번호 검증
     */
    public void verifyPasscode(Authentication authentication, PasscodeVerifyRequest request) {
        Long userId = Long.parseLong(authentication.getName());
        log.info("Verifying passcode for user {}", userId);
        
        // Mock: 간편 비밀번호 검증
        Passcode passcode = passcodeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Passcode not found"));
        
        if (!passcode.getPin().equals(request.getPasscode())) {
            throw new RuntimeException("Invalid passcode");
        }
    }
}
