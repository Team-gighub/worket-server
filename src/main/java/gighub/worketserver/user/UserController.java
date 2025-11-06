package gighub.worketserver.user;

import gighub.worketserver.global.response.ApiResponse;
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

  @PostMapping("users/{userId}/update")
  public ApiResponse<User> updateUser(@PathVariable Long userId,
      @RequestBody UserUpdateRequest request) {
    User updatedUser = userService.updateUser(userId,request);
    return ApiResponse.ok(updatedUser);
  }
}
