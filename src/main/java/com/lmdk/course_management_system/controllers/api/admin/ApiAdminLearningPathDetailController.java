package com.lmdk.course_management_system.controllers.api.admin;

import com.lmdk.course_management_system.dto.admin.assignment.AdminAssignmentOptionResponse;
import com.lmdk.course_management_system.dto.admin.learningpath.AdminLearningPathDetailActionResponse;
import com.lmdk.course_management_system.dto.admin.learningpath.AdminLearningPathDetailPageResponse;
import com.lmdk.course_management_system.dto.admin.learningpath.LearningPathDetailRequest;
import com.lmdk.course_management_system.dto.admin.learningpath.UpdateLearningPathDetailRequest;
import com.lmdk.course_management_system.mappers.admin.AdminLearningPathDetailMapper;
import com.lmdk.course_management_system.pojo.Assignment;
import com.lmdk.course_management_system.pojo.LearningPath;
import com.lmdk.course_management_system.pojo.LearningPathDetail;
import com.lmdk.course_management_system.services.AssignmentService;
import com.lmdk.course_management_system.services.LearningPathDetailService;
import com.lmdk.course_management_system.services.LearningPathService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/learning-path-details")
@RequiredArgsConstructor
public class ApiAdminLearningPathDetailController {

    private final LearningPathDetailService detailService;
    private final LearningPathService learningPathService;
    private final AssignmentService assignmentService;
    private final AdminLearningPathDetailMapper adminLearningPathDetailMapper;

    @Value("${learning-path-details.page-size:10}")
    private int pageSize;

    @GetMapping
    public AdminLearningPathDetailPageResponse getDetails(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer learningPathId
    ) {
        page = Math.max(page, 1);

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));

        if (kw != null && !kw.isBlank())
            params.put("kw", kw.trim());

        if (courseId != null)
            params.put("courseId", String.valueOf(courseId));

        if (learningPathId != null)
            params.put(
                    "learningPathId",
                    String.valueOf(learningPathId)
            );

        long totalRecords =
                detailService.countDetails(params);

        int totalPages = Math.max(
                (int) Math.ceil((double) totalRecords / pageSize),
                1
        );

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        return new AdminLearningPathDetailPageResponse(
                detailService.getDetails(params)
                        .stream()
                        .map(adminLearningPathDetailMapper::toResponse)
                        .toList(),
                page,
                totalPages,
                totalRecords
        );
    }

    @GetMapping("/assignments")
    public List<AdminAssignmentOptionResponse> getAssignments(
            @RequestParam Integer learningPathId
    ) {
        LearningPath learningPath =
                learningPathService
                        .getLearningPathById(learningPathId);

        if (learningPath == null)
            return List.of();

        return assignmentService
                .getAssignmentsByCourse(
                        learningPath.getCourse().getId()
                )
                .stream()
                .map(assignment ->
                        new AdminAssignmentOptionResponse(
                                assignment.getId(),
                                assignment.getName(),
                                assignment.getMaximumScore()
                        )
                )
                .toList();
    }

    @PostMapping
    public AdminLearningPathDetailActionResponse addDetail(
            @RequestBody LearningPathDetailRequest request
    ) {
        LearningPath learningPath =
                learningPathService.getLearningPathById(
                        request.learningPathId()
                );

        if (learningPath == null)
            throw new IllegalArgumentException(
                    "Lộ trình không tồn tại!"
            );

        Assignment assignment =
                assignmentService.getAssignmentById(
                        request.assignmentId()
                );

        if (assignment == null)
            throw new IllegalArgumentException(
                    "Bài tập không tồn tại!"
            );

        LearningPathDetail detail =
                new LearningPathDetail();

        detail.setLearningPath(learningPath);
        detail.setAssignment(assignment);
        detail.setOrderNumber(request.orderNumber());
        detail.setMinimumScore(request.minimumScore());
        detail.setMaxAttempts(request.maxAttempts());

        detailService.addDetail(detail);

        return new AdminLearningPathDetailActionResponse(
                detail.getId(),
                "Thêm bài vào lộ trình thành công!"
        );
    }

    @PutMapping("/{detailId}")
    public AdminLearningPathDetailActionResponse updateDetail(
            @PathVariable Integer detailId,
            @RequestBody UpdateLearningPathDetailRequest request
    ) {
        LearningPathDetail detail =
                detailService.getDetailById(detailId);

        if (detail == null)
            throw new IllegalArgumentException(
                    "Không tìm thấy chi tiết lộ trình!"
            );

        Assignment assignment =
                assignmentService.getAssignmentById(
                        request.assignmentId()
                );

        if (assignment == null)
            throw new IllegalArgumentException(
                    "Bài tập không tồn tại!"
            );

        detail.setAssignment(assignment);
        detail.setOrderNumber(request.orderNumber());
        detail.setMinimumScore(request.minimumScore());
        detail.setMaxAttempts(request.maxAttempts());

        detailService.updateDetail(detail);

        return new AdminLearningPathDetailActionResponse(
                detailId,
                "Cập nhật chi tiết lộ trình thành công!"
        );
    }
}