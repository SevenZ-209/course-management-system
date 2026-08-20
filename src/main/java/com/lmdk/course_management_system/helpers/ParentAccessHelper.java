package com.lmdk.course_management_system.helpers;

import com.lmdk.course_management_system.pojo.ParentLink;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.ParentLinkService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ParentAccessHelper {

    private final ParentLinkService parentLinkService;

    public User requireLinkedStudent(
            User parent,
            Integer studentId
    ) {
        if(parent.getRole() != User.UserRole.PARENT)
            throw new IllegalArgumentException(
                    "Tài khoản không phải phụ huynh!"
            );

        ParentLink link =
                parentLinkService
                        .getParentLinksByParent(parent.getId())
                        .stream()
                        .filter(item ->
                                item.getStudent()
                                        .getId()
                                        .equals(studentId)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Bạn không có quyền xem học viên này!"
                                )
                        );

        return link.getStudent();
    }
}