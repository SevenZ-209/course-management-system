package com.lmdk.course_management_system.selectionux;

import com.lmdk.course_management_system.controllers.UserController;
import com.lmdk.course_management_system.controllers.api.admin.ApiAdminUserController;
import com.lmdk.course_management_system.helpers.CurrentUserHelper;
import com.lmdk.course_management_system.mappers.admin.AdminUserMapper;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.UserService;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SelectionUxBatch3UserLookupTest {

    @Test
    void thymeleafStudentLookup_usesBoundedServerSearchWithoutLoadingAllStudents() {
        UserService userService = mock(UserService.class);
        User student = user(11, "student11", "Học viên 11", User.UserRole.STUDENT);
        when(userService.searchUsersByRole(User.UserRole.STUDENT, "hoc", 1, 20)).thenReturn(List.of(student));

        UserController controller = new UserController(userService);
        var result = controller.getStudentOptions(" hoc ", 0, 20);

        assertEquals(1, result.size());
        assertEquals(11, result.get(0).get("id"));
        verify(userService).searchUsersByRole(User.UserRole.STUDENT, "hoc", 1, 20);
        verify(userService, never()).getUsersByRole(User.UserRole.STUDENT);
    }

    @Test
    void thymeleafParentLookup_clampsSizeAndSearchesOnlyParents() {
        UserService userService = mock(UserService.class);
        User parent = user(21, "parent21", "Phụ huynh 21", User.UserRole.PARENT);
        when(userService.searchUsersByRole(User.UserRole.PARENT, "phu", 1, 50)).thenReturn(List.of(parent));

        UserController controller = new UserController(userService);
        var result = controller.getParentOptions("phu", 0, 999);

        assertEquals(1, result.size());
        assertEquals(21, result.get(0).get("id"));
        verify(userService).searchUsersByRole(User.UserRole.PARENT, "phu", 1, 50);
        verify(userService, never()).getUsersByRole(User.UserRole.PARENT);
    }

    @Test
    void reactAdminParentLookup_usesBoundedServerSearch() {
        UserService userService = mock(UserService.class);
        User parent = user(31, "parent31", "Phụ huynh 31", User.UserRole.PARENT);
        when(userService.searchUsersByRole(User.UserRole.PARENT, "parent", 1, 20)).thenReturn(List.of(parent));

        ApiAdminUserController controller = new ApiAdminUserController(
                userService, mock(CurrentUserHelper.class), new AdminUserMapper());
        var result = controller.getParentOptions(" parent ", 1, 20);

        assertEquals(1, result.size());
        assertEquals(31, result.get(0).id());
        verify(userService).searchUsersByRole(User.UserRole.PARENT, "parent", 1, 20);
        verify(userService, never()).getUsersByRole(User.UserRole.PARENT);
    }

    private User user(Integer id, String username, String fullName, User.UserRole role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setFullName(fullName);
        user.setEmail(username + "@example.com");
        user.setRole(role);
        user.setStatus(User.UserStatus.ACTIVE);
        return user;
    }
}
