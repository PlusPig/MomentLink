package com.keyy.momentlinkback.controller.admin;

import com.keyy.momentlinkback.common.PageResult;
import com.keyy.momentlinkback.common.Result;
import com.keyy.momentlinkback.dto.response.UserResponse;
import com.keyy.momentlinkback.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理员-用户管理", description = "管理员用户管理接口")
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    @Operation(summary = "查询所有用户")
    @GetMapping
    public Result<PageResult<UserResponse>> getAllUsers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(userService.getAllUsers(page, size, keyword, status));
    }

    @Operation(summary = "查询用户详情")
    @GetMapping("/{id}")
    public Result<UserResponse> getUserDetail(@PathVariable Long id) {
        return Result.success(userService.getUserById(id));
    }

    @Operation(summary = "禁用用户")
    @PutMapping("/{id}/disable")
    public Result<Void> disableUser(@PathVariable Long id) {
        userService.disableUser(id);
        return Result.success("用户已禁用", null);
    }

    @Operation(summary = "启用用户")
    @PutMapping("/{id}/enable")
    public Result<Void> enableUser(@PathVariable Long id) {
        userService.enableUser(id);
        return Result.success("用户已启用", null);
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success("用户已删除", null);
    }
}
