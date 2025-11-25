package gighub.worketserver.controller;

import gighub.worketserver.dto.PasscodeDto;
import gighub.worketserver.global.response.ApiResponse;
import gighub.worketserver.service.PasscodeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/passcode")
public class AuthController {

    private final PasscodeService passcodeService;

    @PostMapping("/register")
    public ApiResponse<?> register(
            @Valid @RequestBody PasscodeDto request,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        String passcode = request.getPasscode();

        passcodeService.registerPasscode(userId, passcode);

        return ApiResponse.ok("패스코드 등록 완료");
    }

    @PostMapping("/verify")
    public ApiResponse<?> verify(
            @Valid @RequestBody PasscodeDto request,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());

        passcodeService.verifyPasscode(userId, request.getPasscode());

        return ApiResponse.ok("패스코드 인증 성공");
    }
}
