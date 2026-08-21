package com.lmdk.course_management_system.controllers.api.auth;

import com.lmdk.course_management_system.dto.cloudinary.CloudinaryUploadResult;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.CloudinaryService;
import com.lmdk.course_management_system.services.UserService;
import com.lmdk.course_management_system.utils.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class ApiAuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final CloudinaryService cloudinaryService;

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

    @PostMapping(
            value = "/register",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public ResponseEntity<?> register(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String role,
            @RequestParam MultipartFile avatar
    ) {

        CloudinaryUploadResult uploadResult = null;

        try {

            fullName = fullName.trim();
            email = email.trim();
            username = username.trim();

            if (fullName.isBlank())
                throw new IllegalArgumentException(
                        "Họ và tên không được để trống!"
                );

            if (username.length() < 5)
                throw new IllegalArgumentException(
                        "Tên đăng nhập phải từ 5 ký tự trở lên!"
                );

            if (password.length() < 8)
                throw new IllegalArgumentException(
                        "Mật khẩu phải từ 8 ký tự trở lên!"
                );

            if (!password.matches(".*[0-9].*"))
                throw new IllegalArgumentException(
                        "Mật khẩu phải có ít nhất một chữ số!"
                );

            if (!password.matches(".*[a-z].*"))
                throw new IllegalArgumentException(
                        "Mật khẩu phải có ký tự thường!"
                );

            if (!password.matches(".*[A-Z].*"))
                throw new IllegalArgumentException(
                        "Mật khẩu phải có ký tự hoa!"
                );

            String emailRegex =
                    "^[a-zA-Z0-9_.+-]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$";

            if (!email.matches(emailRegex))
                throw new IllegalArgumentException(
                        "Email không đúng định dạng!"
                );

            if (userService.existsByUsername(username))
                throw new IllegalArgumentException(
                        "Username đã tồn tại!"
                );

            if (userService.existsByEmail(email))
                throw new IllegalArgumentException(
                        "Email đã tồn tại!"
                );


            User.UserRole userRole;

            try {
                userRole = User.UserRole.valueOf(
                        role.trim().toUpperCase()
                );
            } catch (Exception ex) {
                throw new IllegalArgumentException(
                        "Vai trò không hợp lệ!"
                );
            }

            if (userRole != User.UserRole.STUDENT
                    && userRole != User.UserRole.PARENT)
                throw new IllegalArgumentException(
                        "Chỉ có thể đăng ký tài khoản học viên hoặc phụ huynh!"
                );


            uploadResult =
                    cloudinaryService.uploadImage(avatar);


            User user = new User();

            user.setFullName(fullName);
            user.setEmail(email);
            user.setUsername(username);

            // addUser() sẽ encode
            user.setPasswordHash(password);

            user.setRole(userRole);
            user.setStatus(User.UserStatus.ACTIVE);

            user.setAvatar(
                    uploadResult.url()
            );


            User savedUser =
                    userService.addUser(user);


            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            new RegisterResponse(
                                    savedUser.getId(),
                                    savedUser.getUsername(),
                                    savedUser.getFullName(),
                                    savedUser.getEmail(),
                                    savedUser.getAvatar(),
                                    savedUser.getRole().name()
                            )
                    );

        } catch (IllegalArgumentException ex) {

            if (uploadResult != null) {
                try {
                    cloudinaryService.deleteImage(
                            uploadResult.publicId()
                    );
                } catch (Exception ignored) {
                }
            }

            return ResponseEntity
                    .badRequest()
                    .body(
                            new MessageResponse(
                                    ex.getMessage()
                            )
                    );
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
    
    public record RegisterResponse(
            Integer userId,
            String username,
            String fullName,
            String email,
            String avatar,
            String role
    ) {
    }
}