package com.lmdk.course_management_system.mappers.parent;

import com.lmdk.course_management_system.dto.parent.ParentLinkCodeResponse;
import com.lmdk.course_management_system.dto.parent.ParentStudentResponse;
import com.lmdk.course_management_system.dto.student.dashboard.StudentLinkedParentResponse;
import com.lmdk.course_management_system.pojo.ParentLink;
import com.lmdk.course_management_system.pojo.User;

import org.springframework.stereotype.Component;

@Component
public class ParentLinkMapper {

    public ParentLinkCodeResponse toCodeResponse(
            ParentLink parentLink
    ) {
        return new ParentLinkCodeResponse(
                parentLink.getId(),
                parentLink.getVerificationCode(),
                parentLink.getExpiresAt(),
                parentLink.getStatus().name()
        );
    }

    public StudentLinkedParentResponse toLinkedParentResponse(
            ParentLink parentLink
    ) {
        User parent = parentLink.getParent();

        return new StudentLinkedParentResponse(
                parentLink.getId(),
                parent.getId(),
                parent.getFullName(),
                parent.getUsername(),
                parentLink.getUpdatedAt()
        );
    }

    public ParentStudentResponse toStudentResponse(
            ParentLink parentLink
    ) {
        User student =
                parentLink.getStudent();

        return new ParentStudentResponse(
                parentLink.getId(),
                student.getId(),
                student.getFullName(),
                student.getUsername(),
                parentLink.getUpdatedAt()
        );
    }
}