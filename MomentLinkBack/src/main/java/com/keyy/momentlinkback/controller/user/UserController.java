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

@Tag(name = "用户管理", description = "用户个人信息管理")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/me")
    public Result<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {
        return Result.success(userService.getCurrentUser(userDetails.getUsername()));
    }

    @Operation(summary = "获取用户信息")
    @GetMapping("/{id}")
    public Result<UserResponse> getUserById(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @Operation(summary = "更新用户资料")
    @PutMapping("/profile")
    public Result<UserResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateProfileRequest request) {
        return Result.success("更新成功",
                userService.updateProfile(userDetails.getUsername(), request));
    }

    @Operation(summary = "上传头像")
    @PostMapping("/avatar")
    public Result<String> uploadAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        String avatarUrl = userService.uploadAvatar(userDetails.getUsername(), file);
        return Result.success("上传成功", avatarUrl);
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<String> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(userDetails.getUsername(), request);
        return Result.success("密码修改成功");
    }
}
