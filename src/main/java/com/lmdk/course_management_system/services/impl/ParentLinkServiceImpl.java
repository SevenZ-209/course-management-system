package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.ParentLink;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.repository.ParentLinkRepository;
import com.lmdk.course_management_system.services.ParentLinkService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class ParentLinkServiceImpl implements ParentLinkService {

    private final ParentLinkRepository parentLinkRepository;

    private static final String CODE_CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public ParentLink getParentLinkById(Integer id) {
        return parentLinkRepository.getParentLinkById(id);
    }

    @Override
    public ParentLink getParentLinkByCode(String verificationCode) {
        ParentLink parentLink = parentLinkRepository.getParentLinkByCode(
                verificationCode.trim().toUpperCase()
        );

        if (parentLink != null
                && parentLink.getStatus() == ParentLink.ParentLinkStatus.UNUSED
                && !parentLink.getExpiresAt().isAfter(LocalDateTime.now())) {
            parentLink.setStatus(ParentLink.ParentLinkStatus.EXPIRED);
            parentLinkRepository.updateParentLink(parentLink);
        }

        return parentLink;
    }

    @Override
    public ParentLink createParentLink(User student, LocalDateTime expiresAt) {
        parentLinkRepository.expireUnusedLinks(LocalDateTime.now());

        if (student == null)
            throw new IllegalArgumentException("Học viên không tồn tại!");

        if (student.getRole() != User.UserRole.STUDENT)
            throw new IllegalArgumentException("Người dùng được chọn không phải học viên!");

        if (student.getStatus() != User.UserStatus.ACTIVE)
            throw new IllegalArgumentException("Tài khoản học viên không hoạt động!");

        if (expiresAt == null || !expiresAt.isAfter(LocalDateTime.now()))
            throw new IllegalArgumentException("Thời gian hết hạn phải ở trong tương lai!");

        if (parentLinkRepository.existsUnusedLinkByStudent(student.getId()))
            throw new IllegalArgumentException("Học viên này đang có mã liên kết còn hiệu lực!");

        ParentLink parentLink = new ParentLink();
        parentLink.setStudent(student);
        parentLink.setParent(null);
        parentLink.setVerificationCode(generateCode());
        parentLink.setExpiresAt(expiresAt);
        parentLink.setStatus(ParentLink.ParentLinkStatus.UNUSED);

        return parentLinkRepository.addParentLink(parentLink);
    }

    @Override
    public ParentLink useVerificationCode(String verificationCode, User parent) {
        if (verificationCode == null || verificationCode.isBlank())
            throw new IllegalArgumentException("Vui lòng nhập mã liên kết!");

        if (parent == null || parent.getRole() != User.UserRole.PARENT)
            throw new IllegalArgumentException("Tài khoản không phải phụ huynh!");

        if (parent.getStatus() != User.UserStatus.ACTIVE)
            throw new IllegalArgumentException("Tài khoản phụ huynh không hoạt động!");

        ParentLink parentLink = getParentLinkByCode(verificationCode);

        if (parentLink == null)
            throw new IllegalArgumentException("Mã liên kết không tồn tại!");

        if(parentLink.getStatus() == ParentLink.ParentLinkStatus.EXPIRED)
            throw new IllegalArgumentException(
                    "Mã liên kết đã hết hạn!"
            );

        if(parentLink.getStatus() == ParentLink.ParentLinkStatus.USED)
            throw new IllegalArgumentException(
                    "Mã liên kết đã được sử dụng!"
            );

        if(parentLink.getStatus() == ParentLink.ParentLinkStatus.UNLINKED)
            throw new IllegalArgumentException(
                    "Mã liên kết không còn hiệu lực!"
            );

        if(parentLink.getStatus() != ParentLink.ParentLinkStatus.UNUSED)
            throw new IllegalArgumentException(
                    "Mã liên kết không còn hiệu lực!"
            );

        if (parentLinkRepository.existsActiveLink(
                parent.getId(),
                parentLink.getStudent().getId()
        ))
            throw new IllegalArgumentException("Phụ huynh đã liên kết với học viên này!");

        parentLink.setParent(parent);
        parentLink.setStatus(ParentLink.ParentLinkStatus.USED);

        parentLinkRepository.updateParentLink(parentLink);

        return parentLink;
    }

    @Override
    public void expireParentLink(Integer id) {
        ParentLink parentLink = parentLinkRepository.getParentLinkById(id);

        if (parentLink == null)
            throw new IllegalArgumentException("Không tìm thấy mã liên kết!");

        if (parentLink.getStatus() != ParentLink.ParentLinkStatus.UNUSED)
            throw new IllegalArgumentException("Chỉ có thể vô hiệu hóa mã chưa sử dụng!");

        parentLink.setStatus(ParentLink.ParentLinkStatus.EXPIRED);
        parentLinkRepository.updateParentLink(parentLink);
    }

    @Override
    public void unlinkParentLink(
            Integer linkId,
            User parent
    ) {
        if(parent == null
                || parent.getRole() != User.UserRole.PARENT)
            throw new IllegalArgumentException(
                    "Tài khoản không phải phụ huynh!"
            );

        if(parent.getStatus() != User.UserStatus.ACTIVE)
            throw new IllegalArgumentException(
                    "Tài khoản phụ huynh không hoạt động!"
            );

        ParentLink parentLink =
                parentLinkRepository
                        .getParentLinkById(linkId);

        if(parentLink == null)
            throw new IllegalArgumentException(
                    "Liên kết không tồn tại!"
            );

        if(parentLink.getStatus()
                != ParentLink.ParentLinkStatus.USED)
            throw new IllegalArgumentException(
                    "Liên kết không còn hoạt động!"
            );

        if(parentLink.getParent() == null
                || !parentLink.getParent()
                .getId()
                .equals(parent.getId()))
            throw new IllegalArgumentException(
                    "Bạn không có quyền hủy liên kết này!"
            );

        parentLink.setStatus(
                ParentLink.ParentLinkStatus.UNLINKED
        );

        parentLinkRepository
                .updateParentLink(parentLink);
    }

    @Override
    public List<ParentLink> getParentLinks(Map<String, String> params) {
        parentLinkRepository.expireUnusedLinks(LocalDateTime.now());
        return parentLinkRepository.getParentLinks(params);
    }

    @Override
    public List<ParentLink> getParentLinksByParent(Integer parentId) {
        return parentLinkRepository.getParentLinksByParent(parentId);
    }

    @Override
    public List<ParentLink> getParentLinksByStudent(Integer studentId) {
        return parentLinkRepository.getParentLinksByStudent(studentId);
    }

    @Override
    public long countParentLinks(Map<String, String> params) {
        parentLinkRepository.expireUnusedLinks(LocalDateTime.now());
        return parentLinkRepository.countParentLinks(params);
    }

    private String generateCode() {
        String code;

        do {
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < 8; i++)
                builder.append(CODE_CHARS.charAt(RANDOM.nextInt(CODE_CHARS.length())));

            code = builder.toString();
        } while (parentLinkRepository.existsByVerificationCode(code));

        return code;
    }
}