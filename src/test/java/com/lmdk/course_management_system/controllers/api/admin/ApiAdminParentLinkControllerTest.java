package com.lmdk.course_management_system.controllers.api.admin;

import com.lmdk.course_management_system.dto.admin.parentlink.*;
import com.lmdk.course_management_system.mappers.admin.AdminParentLinkMapper;
import com.lmdk.course_management_system.pojo.ParentLink;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.ParentLinkService;
import com.lmdk.course_management_system.services.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiAdminParentLinkControllerTest {

    @Mock private ParentLinkService parentLinkService;
    @Mock private UserService userService;

    private ApiAdminParentLinkController controller;

    @BeforeEach
    void setUp() {
        controller = new ApiAdminParentLinkController(parentLinkService, userService, new AdminParentLinkMapper());
        ReflectionTestUtils.setField(controller, "pageSize", 10);
    }

    @Test
    void getParentLinks_passesFiltersAndReturnsMappedPage() {
        User student = user(1, "student01", "Học viên 01", User.UserRole.STUDENT);
        User parent = user(2, "parent01", "Phụ huynh 01", User.UserRole.PARENT);
        ParentLink link = link(5, student, parent, ParentLink.ParentLinkStatus.USED);

        when(parentLinkService.countParentLinks(anyMap())).thenReturn(1L);
        when(parentLinkService.getParentLinks(anyMap())).thenReturn(List.of(link));

        AdminParentLinkPageResponse result = controller.getParentLinks(1, " abc ", 1, 2, "used");

        assertEquals(1L, result.totalRecords());
        assertEquals(1, result.parentLinks().size());
        assertEquals("USED", result.parentLinks().get(0).status());
        assertEquals("Học viên 01", result.parentLinks().get(0).studentName());
        assertEquals("Phụ huynh 01", result.parentLinks().get(0).parentName());
        verify(parentLinkService).countParentLinks(argThat(p -> "abc".equals(p.get("kw"))
                && "1".equals(p.get("studentId")) && "2".equals(p.get("parentId")) && "USED".equals(p.get("status"))));
    }

    @Test
    void getParentLinks_normalizesPageAboveTotalPages() {
        when(parentLinkService.countParentLinks(anyMap())).thenReturn(1L);
        when(parentLinkService.getParentLinks(anyMap())).thenReturn(List.of());

        AdminParentLinkPageResponse result = controller.getParentLinks(99, null, null, null, null);

        assertEquals(1, result.currentPage());
        verify(parentLinkService).getParentLinks(argThat(p -> "1".equals(p.get("page"))));
    }

    @Test
    void createParentLink_usesSelectedStudentAndReturnsAction() {
        User student = user(3, "student03", "Học viên 03", User.UserRole.STUDENT);
        ParentLink created = link(11, student, null, ParentLink.ParentLinkStatus.UNUSED);
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(1);

        when(userService.getUserById(3)).thenReturn(student);
        when(parentLinkService.createParentLink(student, expiresAt)).thenReturn(created);

        AdminParentLinkActionResponse result = controller.createParentLink(new CreateParentLinkRequest(3, expiresAt));

        assertEquals(11, result.linkId());
        assertEquals("Tạo mã liên kết thành công!", result.message());
        verify(parentLinkService).createParentLink(student, expiresAt);
    }

    @Test
    void expireParentLink_delegatesToService() {
        AdminParentLinkActionResponse result = controller.expireParentLink(9);
        assertEquals(9, result.linkId());
        assertEquals("Đã vô hiệu hóa mã liên kết!", result.message());
        verify(parentLinkService).expireParentLink(9);
    }

    private User user(Integer id, String username, String fullName, User.UserRole role) {
        User user = new User();
        user.setId(id);
        user.setUsername(username);
        user.setFullName(fullName);
        user.setRole(role);
        user.setStatus(User.UserStatus.ACTIVE);
        return user;
    }

    private ParentLink link(Integer id, User student, User parent, ParentLink.ParentLinkStatus status) {
        ParentLink link = new ParentLink();
        link.setId(id);
        link.setStudent(student);
        link.setParent(parent);
        link.setVerificationCode("ABC12345");
        link.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        link.setExpiresAt(LocalDateTime.now().plusDays(1));
        link.setStatus(status);
        return link;
    }
}
