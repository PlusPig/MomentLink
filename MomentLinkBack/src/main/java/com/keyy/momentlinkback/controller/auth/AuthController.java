package com.keyy.momentlinkback.controller.auth;

import com.keyy.momentlinkback.common.Result;
import com.keyy.momentlinkback.dto.request.LoginRequest;
import com.keyy.momentlinkback.dto.request.RegisterRequest;
import com.keyy.momentlinkback.dto.response.AuthResponse;
import com.keyy.momentlinkback.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "认证管理", description = "用户登录注册相关接口")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return Result.success("注册成功", authService.register(request));
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("登录成功", authService.login(request));
    }

    @Operation(summary = "刷新Token")
    @PostMapping("/refresh")
    public Result<AuthResponse> refreshToken(@RequestHeader("Authorization") String token) {
        return Result.success("刷新成功", authService.refreshToken(token));
    }

    @Operation(summary = "退出登录")
    @PostMapping("/logout")
    public Result<Void> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return Result.success("退出成功", null);
    }
}
