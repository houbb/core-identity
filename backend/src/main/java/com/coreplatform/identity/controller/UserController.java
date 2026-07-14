package com.coreplatform.identity.controller;

import com.coreplatform.identity.dto.ChangePasswordRequest;
import com.coreplatform.identity.dto.UpdateUserRequest;
import com.coreplatform.common.response.ApiResponse;
import com.coreplatform.identity.security.JwtAuthenticationToken;
import com.coreplatform.identity.service.UserService;
import com.coreplatform.identity.vo.UserVO;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ApiResponse<UserVO> getProfile(@AuthenticationPrincipal JwtAuthenticationToken auth) {
        UserVO userVO = userService.getById(auth.getUserId());
        return ApiResponse.success(userVO);
    }

    @PutMapping("/profile")
    public ApiResponse<UserVO> updateProfile(@AuthenticationPrincipal JwtAuthenticationToken auth,
                                              @Valid @RequestBody UpdateUserRequest request) {
        UserVO userVO = userService.update(auth.getUserId(), request.getDisplayName(), request.getAvatar());
        return ApiResponse.success(userVO);
    }

    @PutMapping("/profile/password")
    public ApiResponse<Void> changePassword(@AuthenticationPrincipal JwtAuthenticationToken auth,
                                             @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(auth.getUserId(), request.getOldPassword(), request.getNewPassword());
        return ApiResponse.success();
    }
}