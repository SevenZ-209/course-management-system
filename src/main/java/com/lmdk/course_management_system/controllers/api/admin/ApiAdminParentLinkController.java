package com.lmdk.course_management_system.controllers.api.admin;

import com.lmdk.course_management_system.dto.admin.parentlink.*;
import com.lmdk.course_management_system.mappers.admin.AdminParentLinkMapper;
import com.lmdk.course_management_system.pojo.ParentLink;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.ParentLinkService;
import com.lmdk.course_management_system.services.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/parent-links")
@RequiredArgsConstructor
public class ApiAdminParentLinkController {

    private final ParentLinkService parentLinkService;
    private final UserService userService;
    private final AdminParentLinkMapper adminParentLinkMapper;

    @Value("${parent-links.page-size:10}")
    private int pageSize;

    @GetMapping
    public AdminParentLinkPageResponse getParentLinks(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) Integer studentId,
            @RequestParam(required = false) Integer parentId,
            @RequestParam(required = false) String status
    ) {
        page = Math.max(page, 1);

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        if (kw != null && !kw.isBlank()) params.put("kw", kw.trim());
        if (studentId != null) params.put("studentId", String.valueOf(studentId));
        if (parentId != null) params.put("parentId", String.valueOf(parentId));
        if (status != null && !status.isBlank()) params.put("status", status.trim().toUpperCase());

        long totalRecords = parentLinkService.countParentLinks(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        return new AdminParentLinkPageResponse(
                parentLinkService.getParentLinks(params).stream().map(adminParentLinkMapper::toResponse).toList(),
                page, totalPages, totalRecords
        );
    }

    @PostMapping
    public AdminParentLinkActionResponse createParentLink(@RequestBody CreateParentLinkRequest request) {
        if (request == null || request.studentId() == null)
            throw new IllegalArgumentException("Vui lòng chọn học viên!");

        User student = userService.getUserById(request.studentId());
        ParentLink link = parentLinkService.createParentLink(student, request.expiresAt());
        return new AdminParentLinkActionResponse(link.getId(), "Tạo mã liên kết thành công!");
    }

    @PatchMapping("/{linkId}/expire")
    public AdminParentLinkActionResponse expireParentLink(@PathVariable Integer linkId) {
        parentLinkService.expireParentLink(linkId);
        return new AdminParentLinkActionResponse(linkId, "Đã vô hiệu hóa mã liên kết!");
    }
}
