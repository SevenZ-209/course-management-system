package com.lmdk.course_management_system.controllers.api.student;

import com.lmdk.course_management_system.dto.parent.ParentLinkCodeResponse;
import com.lmdk.course_management_system.exceptions.ForbiddenException;
import com.lmdk.course_management_system.helpers.CurrentUserHelper;
import com.lmdk.course_management_system.mappers.parent.ParentLinkMapper;
import com.lmdk.course_management_system.pojo.ParentLink;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.ParentLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/student/parent-links")
@RequiredArgsConstructor
public class ApiStudentParentLinkController {

    private final ParentLinkService parentLinkService;
    private final CurrentUserHelper currentUserHelper;
    private final ParentLinkMapper parentLinkMapper;

    @PostMapping
    public ParentLinkCodeResponse createLink(Authentication authentication) {
        User student = currentUserHelper.getCurrentStudent(authentication);
        ParentLink parentLink = parentLinkService.createParentLink(
                student,
                LocalDateTime.now().plusMinutes(5)
        );

        return parentLinkMapper.toCodeResponse(parentLink);
    }

    @GetMapping("/current")
    public ParentLinkCodeResponse getCurrentLink(Authentication authentication) {
        User student = currentUserHelper.getCurrentStudent(authentication);

        ParentLink parentLink = parentLinkService.getParentLinksByStudent(student.getId())
                .stream()
                .filter(link -> link.getStatus() == ParentLink.ParentLinkStatus.UNUSED
                        && link.getExpiresAt().isAfter(LocalDateTime.now()))
                .findFirst()
                .orElse(null);

        return parentLink == null ? null : parentLinkMapper.toCodeResponse(parentLink);
    }

    @DeleteMapping("/{linkId}")
    public void expireLink(@PathVariable Integer linkId, Authentication authentication) {
        User student = currentUserHelper.getCurrentStudent(authentication);
        ParentLink parentLink = parentLinkService.getParentLinkById(linkId);

        if(parentLink == null || !parentLink.getStudent().getId().equals(student.getId()))
            throw new ForbiddenException("Bạn không có quyền hủy mã liên kết này!");

        parentLinkService.expireParentLink(linkId);
    }
}