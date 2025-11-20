package gighub.worketserver.controller;

import gighub.worketserver.global.response.ApiResponse;
import gighub.worketserver.service.PasscodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/passcode")
public class PasscodeController {

    private final PasscodeService passcodeService;

    @PostMapping("/register")
    public ApiResponse<?> register(
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        String passcode = body.get("passcode");
        System.out.println("패스코드 왓어용" + passcode);

        passcodeService.registerPasscode(userId, passcode);

        return ApiResponse.ok("패스코드 등록 완료");
    }

    @PostMapping("/verify")
    public ApiResponse<?> verify(
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());
        String passcode = body.get("passcode");

        passcodeService.verifyPasscode(userId, passcode);

        return ApiResponse.ok("패스코드 인증 성공");
    }
}
