package gighub.worketserver.controller;

import gighub.worketserver.domain.User;
import gighub.worketserver.dto.UserDetailDto;
import gighub.worketserver.dto.UserProfileDto;
import gighub.worketserver.dto.UserUpdateRequest;
import gighub.worketserver.global.exception.CommonErrorCode;
import gighub.worketserver.global.exception.RestApiException;
import gighub.worketserver.global.response.ApiResponse;
import gighub.worketserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 사용자(User) 관련 API Controller
 */
@RestController
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/mypage")
  public ApiResponse<UserProfileDto> getMyPage(Authentication authentication) {

    Long userId = Long.valueOf(authentication.getName());

    UserProfileDto dto = userService.getUser(userId);

    return ApiResponse.ok(dto);
  }

  @GetMapping("/test")
  public ApiResponse<String> test(Authentication authentication) {
//    throw new RestApiException(CommonErrorCode.BAD_REQUEST, "테스트로 예외를 발생시켯습니다~");
    return ApiResponse.ok("hi");
  }
}
