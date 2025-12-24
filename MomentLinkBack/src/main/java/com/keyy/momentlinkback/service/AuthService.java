package com.keyy.momentlinkback.service;

import com.keyy.momentlinkback.dto.request.LoginRequest;
import com.keyy.momentlinkback.dto.request.RegisterRequest;
import com.keyy.momentlinkback.dto.response.AuthResponse;
import com.keyy.momentlinkback.dto.response.UserResponse;
import com.keyy.momentlinkback.entity.User;
import com.keyy.momentlinkback.exception.BadRequestException;
import com.keyy.momentlinkback.exception.UnauthorizedException;
import com.keyy.momentlinkback.repository.UserRepository;
import com.keyy.momentlinkback.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 检查用户名是否存在
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("用户名已存在");
        }

        // 检查邮箱是否存在
        if (request.getEmail() != null &&
                userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("邮箱已被使用");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setNickname(request.getNickname() != null ?
                request.getNickname() : request.getUsername());
        user.setStatus(1);
        user.setRole(0);
        user.setDeleted(0);

        user = userRepository.save(user);

        // 生成 Token
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        String token = jwtTokenProvider.generateToken(authentication);
        UserResponse userResponse = userService.convertToResponse(user);

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .user(userResponse)
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            String token = jwtTokenProvider.generateToken(authentication);
            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UnauthorizedException("用户不存在"));

            UserResponse userResponse = userService.convertToResponse(user);

            return AuthResponse.builder()
                    .token(token)
                    .type("Bearer")
                    .user(userResponse)
                    .build();
        } catch (Exception e) {
            throw new UnauthorizedException("用户名或密码错误");
        }
    }

    public AuthResponse refreshToken(String oldToken) {
        // 去除 Bearer 前缀
        String token = oldToken.replace("Bearer ", "");

        if (!jwtTokenProvider.validateToken(token)) {
            throw new UnauthorizedException("Token无效");
        }

        String username = jwtTokenProvider.getUsernameFromToken(token);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("用户不存在"));

        // 生成新Token
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                username, null, null);
        String newToken = jwtTokenProvider.generateToken(authentication);

        UserResponse userResponse = userService.convertToResponse(user);

        return AuthResponse.builder()
                .token(newToken)
                .type("Bearer")
                .user(userResponse)
                .build();
    }

    public void logout(String token) {
        // 可以将token加入黑名单(Redis)
        // 这里简化处理,客户端直接删除token即可
    }
}
