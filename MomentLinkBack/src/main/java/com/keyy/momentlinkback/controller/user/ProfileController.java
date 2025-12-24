package com.keyy.momentlinkback.controller.user;

import com.keyy.momentlinkback.common.Result;
import com.keyy.momentlinkback.dto.request.UpdatePasswordRequest;
import com.keyy.momentlinkback.dto.request.UpdateProfileRequest;
import com.keyy.momentlinkback.dto.response.UserResponse;
import com.keyy.momentlinkback.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "个人资料管理", description = "用户个人资料相关接口")
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserService userService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping
    public Result<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse user = userService.getCurrentUser(userDetails.getUsername());
        return Result.success(user);
    }

    @Operation(summary = "更新个人资料")
    @PutMapping
    public Result<UserResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        UserResponse user = userService.updateProfile(userDetails.getUsername(), request);
        return Result.success(user);
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        String avatarUrl = userService.uploadAvatar(userDetails.getUsername(), file);
        return Result.success(avatarUrl);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(userDetails.getUsername(), request);
        return Result.success();
    }

    @Operation(summary = "获取用户统计信息")
    @GetMapping("/stats")
    public Result<Object> getUserStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        // TODO: 实现用户统计信息
        return Result.success();
    }
}
