package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.pojo.ParentLink;
import com.lmdk.course_management_system.pojo.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ParentLinkService {

    ParentLink getParentLinkById(Integer id);

    ParentLink getParentLinkByCode(String verificationCode);

    ParentLink getCurrentUnusedLinkByStudent(Integer studentId);

    ParentLink createParentLink(User student, LocalDateTime expiresAt);

    ParentLink useVerificationCode(String verificationCode, User parent);

    void expireParentLink(Integer id);

    List<ParentLink> getParentLinks(Map<String, String> params);

    List<ParentLink> getParentLinksByParent(Integer parentId);

    List<ParentLink> getParentLinksByStudent(Integer studentId);

    long countParentLinks(Map<String, String> params);

    void unlinkParentLink(
            Integer linkId,
            User parent
    );

    void unlinkParentLinkByStudent(
            Integer linkId,
            User student
    );
}