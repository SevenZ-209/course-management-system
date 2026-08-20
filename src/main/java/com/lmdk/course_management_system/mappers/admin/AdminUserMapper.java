package com.lmdk.course_management_system.mappers.admin;

import com.lmdk.course_management_system.dto.admin.user.AdminTeacherOptionResponse;
import com.lmdk.course_management_system.dto.admin.user.AdminUserResponse;
import com.lmdk.course_management_system.pojo.User;

import org.springframework.stereotype.Component;

@Component
public class AdminUserMapper {

    public AdminUserResponse toResponse(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt()
        );
    }

    public AdminTeacherOptionResponse toTeacherOptionResponse(
            User user
    ) {
        return new AdminTeacherOptionResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName()
        );
    }
}