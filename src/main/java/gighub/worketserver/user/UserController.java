package gighub.worketserver.user;

import gighub.worketserver.global.response.ApiResponse;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/users")
    public ApiResponse<List<User>> findAll() {
        return ApiResponse.ok(userService.findAllUsers());
    }

    @GetMapping("/mypage")
    public ApiResponse<UserProfileDto> getMyPage(Authentication authentication) {
        try {
            String userId = authentication.getName(); // JWT의 sub
            System.out.println("유저아이디띠디디디" + userId);
            UserProfileDto profile = userService.getUser(userId);
            return ApiResponse.ok(profile);
        } catch (Exception e) {
            return ApiResponse.error("마이페이지 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @PostMapping("users/{userId}/update")
    public ApiResponse<User> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateRequest request
    ) {
        User updatedUser = userService.updateUser(userId, request);
        return ApiResponse.ok(updatedUser);
    }
}
