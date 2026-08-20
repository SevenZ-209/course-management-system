package com.lmdk.course_management_system.controllers.api.student;

import com.lmdk.course_management_system.dto.parent.CreateParentLinkRequest;
import com.lmdk.course_management_system.dto.parent.ParentLinkCodeResponse;
import com.lmdk.course_management_system.helpers.CurrentUserHelper;
import com.lmdk.course_management_system.mappers.parent.ParentLinkMapper;
import com.lmdk.course_management_system.pojo.ParentLink;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.ParentLinkService;

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/student/parent-links")
@RequiredArgsConstructor
public class ApiStudentParentLinkController {

    private final ParentLinkService parentLinkService;
    private final CurrentUserHelper currentUserHelper;
    private final ParentLinkMapper parentLinkMapper;

    @PostMapping
    public ParentLinkCodeResponse createLink(
            @RequestBody CreateParentLinkRequest request,
            Authentication authentication
    ) {
        User student =
                currentUserHelper.getCurrentStudent(
                        authentication
                );

        ParentLink parentLink =
                parentLinkService.createParentLink(
                        student,
                        request.expiresAt()
                );

        return parentLinkMapper
                .toCodeResponse(parentLink);
    }
}