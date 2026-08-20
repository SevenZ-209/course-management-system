package com.lmdk.course_management_system.helpers;

import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserHelper {

    private final UserService userService;

    public User getCurrentUser(
            Authentication authentication
    ) {
        if(authentication == null
                || authentication.getName() == null)
            throw new IllegalArgumentException(
                    "Không xác định được tài khoản!"
            );

        User user =
                userService.getUserByUsername(
                        authentication.getName()
                );

        if(user == null)
            throw new IllegalArgumentException(
                    "Tài khoản không tồn tại!"
            );

        if(user.getStatus() != User.UserStatus.ACTIVE)
            throw new IllegalArgumentException(
                    "Tài khoản không hoạt động!"
            );

        return user;
    }

    public User getCurrentStudent(
            Authentication authentication
    ) {
        User student =
                getCurrentUser(authentication);

        if(student.getRole() != User.UserRole.STUDENT)
            throw new IllegalArgumentException(
                    "Tài khoản không phải học viên!"
            );

        return student;
    }

    public User getCurrentParent(
            Authentication authentication
    ) {
        User parent =
                getCurrentUser(authentication);

        if(parent.getRole() != User.UserRole.PARENT)
            throw new IllegalArgumentException(
                    "Tài khoản không phải phụ huynh!"
            );

        return parent;
    }
}