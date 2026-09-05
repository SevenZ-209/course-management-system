package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.exceptions.ForbiddenException;
import com.lmdk.course_management_system.pojo.ParentLink;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.repository.ParentLinkRepository;
import com.lmdk.course_management_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ParentLinkServiceImplRegressionTest {

    @Mock private ParentLinkRepository parentLinkRepository;
    @Mock private UserRepository userRepository;

    private ParentLinkServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ParentLinkServiceImpl(parentLinkRepository, userRepository);
    }

    @Test
    void createParentLink_successCreatesUnusedCodeForLockedStudent() {
        User student = user(1, User.UserRole.STUDENT);
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(5);

        when(userRepository.getUserByIdForUpdate(1)).thenReturn(student);
        when(parentLinkRepository.existsUnusedLinkByStudent(1)).thenReturn(false);
        when(parentLinkRepository.existsByVerificationCode(anyString())).thenReturn(false);
        when(parentLinkRepository.addParentLink(any(ParentLink.class))).thenAnswer(i -> i.getArgument(0));

        ParentLink result = service.createParentLink(student, expiresAt);

        assertSame(student, result.getStudent());
        assertNull(result.getParent());
        assertEquals(ParentLink.ParentLinkStatus.UNUSED, result.getStatus());
        assertEquals(expiresAt, result.getExpiresAt());
        assertNotNull(result.getVerificationCode());
        assertEquals(8, result.getVerificationCode().length());

        verify(parentLinkRepository).expireUnusedLinks(any(LocalDateTime.class));
        verify(userRepository).getUserByIdForUpdate(1);
        verify(parentLinkRepository).addParentLink(result);
    }

    @Test
    void createParentLink_blocksSecondValidUnusedCode() {
        User student = user(1, User.UserRole.STUDENT);

        when(userRepository.getUserByIdForUpdate(1)).thenReturn(student);
        when(parentLinkRepository.existsUnusedLinkByStudent(1)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.createParentLink(student, LocalDateTime.now().plusMinutes(5))
        );

        assertEquals("Học viên này đang có mã liên kết còn hiệu lực!", ex.getMessage());
        verify(parentLinkRepository, never()).addParentLink(any());
    }

    @Test
    void useVerificationCode_successNormalizesCodeAndMarksUsed() {
        User student = user(1, User.UserRole.STUDENT);
        User parent = user(3, User.UserRole.PARENT);
        ParentLink link = unusedLink(10, student, "ABCD1234", LocalDateTime.now().plusMinutes(5));

        when(parentLinkRepository.getParentLinkByCodeForUpdate("ABCD1234")).thenReturn(link);
        when(parentLinkRepository.existsActiveLink(3, 1)).thenReturn(false);

        ParentLink result = service.useVerificationCode("  abcd1234  ", parent);

        assertSame(link, result);
        assertSame(parent, result.getParent());
        assertEquals(ParentLink.ParentLinkStatus.USED, result.getStatus());
        verify(parentLinkRepository).getParentLinkByCodeForUpdate("ABCD1234");
        verify(parentLinkRepository).updateParentLink(link);
    }

    @Test
    void useVerificationCode_blocksReusingUsedCode() {
        User student = user(1, User.UserRole.STUDENT);
        User parent = user(3, User.UserRole.PARENT);
        ParentLink link = unusedLink(10, student, "ABCD1234", LocalDateTime.now().plusMinutes(5));
        link.setStatus(ParentLink.ParentLinkStatus.USED);

        when(parentLinkRepository.getParentLinkByCodeForUpdate("ABCD1234")).thenReturn(link);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.useVerificationCode("ABCD1234", parent)
        );

        assertEquals("Mã liên kết đã được sử dụng!", ex.getMessage());
        verify(parentLinkRepository, never()).updateParentLink(any());
    }

    @Test
    void useVerificationCode_blocksExpiredCode() {
        User student = user(1, User.UserRole.STUDENT);
        User parent = user(3, User.UserRole.PARENT);
        ParentLink link = unusedLink(10, student, "ABCD1234", LocalDateTime.now().minusSeconds(1));

        when(parentLinkRepository.getParentLinkByCodeForUpdate("ABCD1234")).thenReturn(link);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.useVerificationCode("ABCD1234", parent)
        );

        assertEquals("Mã liên kết đã hết hạn!", ex.getMessage());
        verify(parentLinkRepository, never()).updateParentLink(any());
    }

    @Test
    void useVerificationCode_blocksDuplicateParentStudentActiveLink() {
        User student = user(1, User.UserRole.STUDENT);
        User parent = user(3, User.UserRole.PARENT);
        ParentLink link = unusedLink(10, student, "ABCD1234", LocalDateTime.now().plusMinutes(5));

        when(parentLinkRepository.getParentLinkByCodeForUpdate("ABCD1234")).thenReturn(link);
        when(parentLinkRepository.existsActiveLink(3, 1)).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.useVerificationCode("ABCD1234", parent)
        );

        assertEquals("Phụ huynh đã liên kết với học viên này!", ex.getMessage());
        assertEquals(ParentLink.ParentLinkStatus.UNUSED, link.getStatus());
        assertNull(link.getParent());
        verify(parentLinkRepository, never()).updateParentLink(any());
    }

    @Test
    void unlinkParentLink_ownerCanUnlinkAndHistoryIsPreserved() {
        User student = user(1, User.UserRole.STUDENT);
        User parent = user(3, User.UserRole.PARENT);
        ParentLink link = usedLink(10, student, parent);

        when(parentLinkRepository.getParentLinkByIdForUpdate(10)).thenReturn(link);

        service.unlinkParentLink(10, parent);

        assertEquals(ParentLink.ParentLinkStatus.UNLINKED, link.getStatus());
        assertSame(parent, link.getParent());
        assertSame(student, link.getStudent());
        verify(parentLinkRepository).updateParentLink(link);
    }

    @Test
    void unlinkParentLink_foreignParentIsForbidden() {
        User student = user(1, User.UserRole.STUDENT);
        User owner = user(3, User.UserRole.PARENT);
        User anotherParent = user(4, User.UserRole.PARENT);
        ParentLink link = usedLink(10, student, owner);

        when(parentLinkRepository.getParentLinkByIdForUpdate(10)).thenReturn(link);

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> service.unlinkParentLink(10, anotherParent)
        );

        assertEquals("Bạn không có quyền hủy liên kết này!", ex.getMessage());
        assertEquals(ParentLink.ParentLinkStatus.USED, link.getStatus());
        verify(parentLinkRepository, never()).updateParentLink(any());
    }

    @Test
    void unlinkParentLinkByStudent_ownerStudentCanRevokeParent() {
        User student = user(1, User.UserRole.STUDENT);
        User parent = user(3, User.UserRole.PARENT);
        ParentLink link = usedLink(10, student, parent);

        when(parentLinkRepository.getParentLinkByIdForUpdate(10)).thenReturn(link);

        service.unlinkParentLinkByStudent(10, student);

        assertEquals(ParentLink.ParentLinkStatus.UNLINKED, link.getStatus());
        verify(parentLinkRepository).updateParentLink(link);
    }

    @Test
    void unlinkParentLinkByStudent_foreignStudentIsForbidden() {
        User ownerStudent = user(1, User.UserRole.STUDENT);
        User anotherStudent = user(2, User.UserRole.STUDENT);
        User parent = user(3, User.UserRole.PARENT);
        ParentLink link = usedLink(10, ownerStudent, parent);

        when(parentLinkRepository.getParentLinkByIdForUpdate(10)).thenReturn(link);

        ForbiddenException ex = assertThrows(
                ForbiddenException.class,
                () -> service.unlinkParentLinkByStudent(10, anotherStudent)
        );

        assertEquals("Bạn không có quyền hủy liên kết này!", ex.getMessage());
        assertEquals(ParentLink.ParentLinkStatus.USED, link.getStatus());
        verify(parentLinkRepository, never()).updateParentLink(any());
    }

    @Test
    void getParentLinkByCode_marksExpiredUnusedCodeAsExpired() {
        User student = user(1, User.UserRole.STUDENT);
        ParentLink link = unusedLink(10, student, "ABCD1234", LocalDateTime.now().minusSeconds(1));

        when(parentLinkRepository.getParentLinkByCode("ABCD1234")).thenReturn(link);

        ParentLink result = service.getParentLinkByCode("  abcd1234 ");

        assertSame(link, result);
        assertEquals(ParentLink.ParentLinkStatus.EXPIRED, result.getStatus());
        verify(parentLinkRepository).updateParentLink(link);
    }

    @Test
    void getCurrentUnusedLink_expiresOldCodesBeforeReadingCurrentOne() {
        User student = user(1, User.UserRole.STUDENT);
        ParentLink current = unusedLink(11, student, "EFGH5678", LocalDateTime.now().plusMinutes(4));

        when(parentLinkRepository.getUnusedLinkByStudent(eq(1), any(LocalDateTime.class))).thenReturn(current);

        ParentLink result = service.getCurrentUnusedLinkByStudent(1);

        assertSame(current, result);
        verify(parentLinkRepository).expireUnusedLinks(any(LocalDateTime.class));
        verify(parentLinkRepository).getUnusedLinkByStudent(eq(1), any(LocalDateTime.class));
    }

    private User user(Integer id, User.UserRole role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setStatus(User.UserStatus.ACTIVE);
        return user;
    }

    private ParentLink unusedLink(Integer id, User student, String code, LocalDateTime expiresAt) {
        ParentLink link = new ParentLink();
        link.setId(id);
        link.setStudent(student);
        link.setVerificationCode(code);
        link.setExpiresAt(expiresAt);
        link.setStatus(ParentLink.ParentLinkStatus.UNUSED);
        return link;
    }

    private ParentLink usedLink(Integer id, User student, User parent) {
        ParentLink link = unusedLink(id, student, "ABCD1234", LocalDateTime.now().plusMinutes(5));
        link.setParent(parent);
        link.setStatus(ParentLink.ParentLinkStatus.USED);
        return link;
    }
}
