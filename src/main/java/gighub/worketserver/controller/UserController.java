package gighub.worketserver.controller;

import gighub.worketserver.domain.User;
import gighub.worketserver.dto.UserDetailDto;
import gighub.worketserver.dto.UserProfileDto;
import gighub.worketserver.dto.UserUpdateRequest;
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

    /**
     * 전체 유저 조회 (관리자용)
     * GET /users
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ApiResponse<List<User>> findAll() {
        return ApiResponse.ok(userService.findAllUsers());
    }

    /**
     * 마이페이지 조회
     * GET /mypage
     */
    @GetMapping("/mypage")
    public ApiResponse<UserProfileDto> getMyPage(Authentication authentication) {
        try {
            String userId = authentication.getName(); // JWT 토큰에서 sub(user_id) 끌어옴
            UserProfileDto profile = userService.getUser(Long.parseLong(userId));
            return ApiResponse.ok(profile);
        } catch (Exception e) {
            return ApiResponse.error("마이페이지 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
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
}
