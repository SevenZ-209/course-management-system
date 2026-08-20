package com.lmdk.course_management_system.controllers.api.admin;

import com.lmdk.course_management_system.dto.admin.user.*;
import com.lmdk.course_management_system.helpers.CurrentUserHelper;
import com.lmdk.course_management_system.mappers.admin.AdminUserMapper;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class ApiAdminUserController {

    private final UserService userService;
    private final CurrentUserHelper currentUserHelper;
    private final AdminUserMapper adminUserMapper;

    @Value("${users.page-size:10}")
    private int pageSize;

    @GetMapping
    public AdminUserPageResponse getUsers(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status
    ) {
        page = Math.max(page, 1);

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));

        if(kw != null && !kw.isBlank())
            params.put("kw", kw.trim());

        if(role != null && !role.isBlank())
            params.put("role", role);

        if(status != null && !status.isBlank())
            params.put("status", status);

        long totalRecords = userService.countUsers(params);
        int totalPages = Math.max(
                (int) Math.ceil((double) totalRecords / pageSize),
                1
        );

        if(page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        return new AdminUserPageResponse(
                userService.getUsers(params)
                        .stream()
                        .map(adminUserMapper::toResponse)
                        .toList(),
                page,
                totalPages,
                totalRecords
        );
    }

    @PatchMapping("/{userId}/role")
    public AdminUserActionResponse updateRole(
            @PathVariable Integer userId,
            @RequestBody UpdateUserRoleRequest request,
            Authentication authentication
    ) {
        User currentUser =
                currentUserHelper.getCurrentUser(authentication);

        User user = requireUser(userId);

        if(user.getId().equals(currentUser.getId()))
            throw new IllegalArgumentException(
                    "Bạn không thể tự thay đổi quyền của mình!"
            );

        if(request.role() == null || request.role().isBlank())
            throw new IllegalArgumentException(
                    "Quyền không được để trống!"
            );

        try {
            user.setRole(
                    User.UserRole.valueOf(
                            request.role().trim().toUpperCase()
                    )
            );
        } catch(IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Quyền không hợp lệ!"
            );
        }

        userService.updateUser(user);

        return new AdminUserActionResponse(
                userId,
                "Cập nhật quyền thành công!"
        );
    }

    @GetMapping("/teacher-options")
    public List<AdminTeacherOptionResponse> getTeacherOptions() {
        return userService
                .getUsersByRole(User.UserRole.TEACHER)
                .stream()
                .filter(user ->
                        user.getStatus()
                                == User.UserStatus.ACTIVE
                )
                .map(adminUserMapper::toTeacherOptionResponse)
                .toList();
    }

    @GetMapping("/student-options")
    public List<AdminStudentOptionResponse> getStudentOptions() {
        return userService
                .getUsersByRole(User.UserRole.STUDENT)
                .stream()
                .map(user ->
                        new AdminStudentOptionResponse(
                                user.getId(),
                                user.getUsername(),
                                user.getFullName(),
                                user.getStatus().name()
                        )
                )
                .toList();
    }

    @PatchMapping("/{userId}/status")
    public AdminUserActionResponse updateStatus(
            @PathVariable Integer userId,
            @RequestBody UpdateUserStatusRequest request,
            Authentication authentication
    ) {
        User currentUser =
                currentUserHelper.getCurrentUser(authentication);

        User user = requireUser(userId);

        if(user.getId().equals(currentUser.getId()))
            throw new IllegalArgumentException(
                    "Bạn không thể tự thay đổi trạng thái của mình!"
            );

        if(request.status() == null || request.status().isBlank())
            throw new IllegalArgumentException(
                    "Trạng thái không được để trống!"
            );

        try {
            user.setStatus(
                    User.UserStatus.valueOf(
                            request.status().trim().toUpperCase()
                    )
            );
        } catch(IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Trạng thái không hợp lệ!"
            );
        }

        userService.updateUser(user);

        return new AdminUserActionResponse(
                userId,
                "Cập nhật trạng thái thành công!"
        );
    }

    private User requireUser(Integer userId) {
        User user = userService.getUserById(userId);

        if(user == null)
            throw new IllegalArgumentException(
                    "Không tìm thấy người dùng!"
            );

        return user;
    }
}