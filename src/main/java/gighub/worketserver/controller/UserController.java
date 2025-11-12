package gighub.worketserver.controller;

import gighub.worketserver.domain.User;
import gighub.worketserver.dto.UserProfileDto;
import gighub.worketserver.global.response.ApiResponse;
import gighub.worketserver.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    public ApiResponse<List<User>> findAll() {
        return ApiResponse.ok(userService.findAllUsers());
    }

    @GetMapping("/mypage")
    public ApiResponse<UserProfileDto> getMyPage(Authentication authentication) {
        try {
            String userId = authentication.getName(); // JWT 토큰에서 sub(user_id) 끌어옴
            UserProfileDto profile = userService.getUser(userId);
            System.out.println("왜안되냐" + userId);
            return ApiResponse.ok(profile);
        } catch (Exception e) {
            return ApiResponse.error("마이페이지 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
