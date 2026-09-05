package com.lmdk.course_management_system.mappers.admin;

import com.lmdk.course_management_system.dto.admin.parentlink.AdminParentLinkResponse;
import com.lmdk.course_management_system.pojo.ParentLink;
import com.lmdk.course_management_system.pojo.User;

import org.springframework.stereotype.Component;

@Component
public class AdminParentLinkMapper {

    public AdminParentLinkResponse toResponse(ParentLink link) {
        User student = link.getStudent();
        User parent = link.getParent();

        return new AdminParentLinkResponse(
                link.getId(), link.getVerificationCode(),
                student.getId(), student.getFullName(), student.getUsername(),
                parent != null ? parent.getId() : null,
                parent != null ? parent.getFullName() : null,
                parent != null ? parent.getUsername() : null,
                link.getCreatedAt(), link.getExpiresAt(), link.getStatus().name()
        );
    }
}
