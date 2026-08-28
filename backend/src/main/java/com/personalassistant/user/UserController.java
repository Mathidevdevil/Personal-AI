package com.personalassistant.user;

import com.personalassistant.common.ApiResponse;
import com.personalassistant.common.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> getProfile() {
        String userId = SecurityUtils.getCurrentUserId();
        UserDto profile = userService.getUserProfile(userId);
        return ResponseEntity.ok(ApiResponse.success(profile));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserDto>> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        UserDto profile = userService.updateProfile(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", profile));
    }

    @PutMapping("/currency")
    public ResponseEntity<ApiResponse<UserDto>> updateCurrency(@Valid @RequestBody UpdateCurrencyRequest request) {
        String userId = SecurityUtils.getCurrentUserId();
        UserDto profile = userService.updateCurrency(userId, request.getCurrency());
        return ResponseEntity.ok(ApiResponse.success("Currency updated successfully", profile));
    }
}
