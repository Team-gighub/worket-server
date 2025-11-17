package gighub.worketserver.controller;

import gighub.worketserver.dto.PasscodeRegisterRequest;
import gighub.worketserver.dto.PasscodeVerifyRequest;
import gighub.worketserver.global.response.ApiResponse;
import gighub.worketserver.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API Controller
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

  private final AuthService authService;

  /**
   * 간편 비밀번호 등록
   * POST /auth/passcode/register
   */
  @PostMapping("/passcode/register")
  @ResponseStatus(HttpStatus.CREATED)
  public ApiResponse<Void> registerPasscode(
    Authentication authentication,
    @RequestBody PasscodeRegisterRequest request
  ) {
    authService.registerPasscode(authentication, request);
    return ApiResponse.ok(null);
  }

  /**
   * 간편 비밀번호 검증
   * POST /auth/passcode/verify
   */
  @PostMapping("/passcode/verify")
  public ApiResponse<Void> verifyPasscode(
    Authentication authentication,
    @RequestBody PasscodeVerifyRequest request
  ) {
    authService.verifyPasscode(authentication, request);
    return ApiResponse.ok(null);
  }
}
