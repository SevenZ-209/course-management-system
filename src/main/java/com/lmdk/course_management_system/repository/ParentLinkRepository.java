package com.lmdk.course_management_system.repository;

import com.lmdk.course_management_system.pojo.ParentLink;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface ParentLinkRepository {

    ParentLink getParentLinkById(Integer id);

    ParentLink getParentLinkByCode(String verificationCode);

    ParentLink addParentLink(ParentLink parentLink);

    void updateParentLink(ParentLink parentLink);

    List<ParentLink> getParentLinks(Map<String, String> params);

    List<ParentLink> getParentLinksByParent(Integer parentId);

    List<ParentLink> getParentLinksByStudent(Integer studentId);

    long countParentLinks(Map<String, String> params);

    boolean existsByVerificationCode(String verificationCode);

    boolean existsActiveLink(Integer parentId, Integer studentId);

    boolean existsUnusedLinkByStudent(Integer studentId);

    int expireUnusedLinks(LocalDateTime now);
}