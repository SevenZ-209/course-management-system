package com.lmdk.course_management_system.controllers.api.auth;

import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.UserService;
import com.lmdk.course_management_system.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ApiAuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        User user = userService.getUserByUsername(request.username());

        if (user == null || !passwordEncoder.matches(request.password(), user.getPasswordHash()))
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new MessageResponse("Sai tên đăng nhập hoặc mật khẩu!"));

        if (user.getStatus() != User.UserStatus.ACTIVE)
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new MessageResponse("Tài khoản không hoạt động!"));

        try {
            String token = jwtUtils.generateToken(
                    user.getUsername(),
                    user.getRole().name()
            );

            return ResponseEntity.ok(new LoginResponse(
                    token,
                    user.getId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getRole().name()
            ));
        } catch (Exception ex) {
            return ResponseEntity.internalServerError()
                    .body(new MessageResponse("Không thể tạo token!"));
        }
    }

    public record LoginRequest(
            String username,
            String password
    ) {
    }

    public record LoginResponse(
            String token,
            Integer userId,
            String username,
            String fullName,
            String role
    ) {
    }

    public record MessageResponse(
            String message
    ) {
    }
}