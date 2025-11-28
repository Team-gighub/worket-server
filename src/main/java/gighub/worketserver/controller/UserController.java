package gighub.worketserver.controller;

import gighub.worketserver.dto.UserDetailDto;
import gighub.worketserver.dto.UserProfileResponse;
import gighub.worketserver.dto.UserProfileRequest;
import gighub.worketserver.dto.UserUpdateRequest;
import gighub.worketserver.global.response.ApiResponse;
import gighub.worketserver.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * 사용자(User) 관련 API Controller
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

  private final UserService userService;

  // 프리랜서 프로필을 생성/갱신 하기 위한 API
  @PostMapping("/me")
  public ApiResponse<UserProfileResponse> createOrUpdateProfile(
    @RequestBody UserProfileRequest request,
    Authentication authentication
  ) {
    Long userId = Long.parseLong(authentication.getName());
    UserProfileResponse dto = userService.createOrUpdateProfile(userId, request);
    return ApiResponse.ok(dto);
  }

  // 프리랜서 프로필을 조회 하기 위한 API
  @GetMapping("/me")
  public ApiResponse<UserProfileResponse> getMyProfile(Authentication authentication) {
    Long userId = Long.parseLong(authentication.getName());
    UserProfileResponse dto = userService.getUserProfile(userId);
    return ApiResponse.ok(dto);
  }

  /**
   * 유저 정보 조회
   * GET /users/{userId}
   */
  @GetMapping("/users/{userId}")
  public ApiResponse<UserDetailDto> getUserDetail(
    Authentication authentication,
    @PathVariable Long userId
  ) {
    UserDetailDto userDetail = userService.getUserDetail(userId);
    return ApiResponse.ok(userDetail);
  }

  /**
   * 유저 정보 수정
   * POST /users/{userId}
   */
  @PostMapping("/users/{userId}")
  public ApiResponse<Void> updateUser(
    Authentication authentication,
    @PathVariable Long userId,
    @RequestBody UserUpdateRequest request
  ) {
    userService.updateUser(userId, request);
    return ApiResponse.ok(null);
  }

  @GetMapping("test")
  public ApiResponse<?> test(Authentication authentication) {
    return ApiResponse.ok(null);
  }
}
