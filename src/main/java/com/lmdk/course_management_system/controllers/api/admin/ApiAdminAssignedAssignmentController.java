package com.lmdk.course_management_system.controllers.api.admin;

import com.lmdk.course_management_system.dto.admin.learningpath.*;
import com.lmdk.course_management_system.mappers.admin.AdminAssignedAssignmentMapper;
import com.lmdk.course_management_system.pojo.AssignedAssignment;
import com.lmdk.course_management_system.pojo.Assignment;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.AssignedAssignmentService;
import com.lmdk.course_management_system.services.AssignmentService;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/assigned-assignments")
@RequiredArgsConstructor
public class ApiAdminAssignedAssignmentController {

    private final AssignedAssignmentService assignedAssignmentService;
    private final EnrollmentService enrollmentService;
    private final AssignmentService assignmentService;
    private final UserService userService;
    private final AdminAssignedAssignmentMapper adminAssignedAssignmentMapper;

    @Value("${assigned-assignments.page-size:10}")
    private int pageSize;

    @GetMapping
    public AdminAssignedAssignmentPageResponse getAssignedAssignments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer learningPathId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String date
    ) {
        page = Math.max(page, 1);

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));

        if(kw != null && !kw.isBlank())
            params.put("kw", kw.trim());

        if(courseId != null)
            params.put("courseId", String.valueOf(courseId));

        if(learningPathId != null)
            params.put(
                    "learningPathId",
                    String.valueOf(learningPathId)
            );

        if(status != null && !status.isBlank())
            params.put("status", status);

        if(date != null && !date.isBlank())
            params.put("date", date);

        long totalRecords =
                assignedAssignmentService
                        .countAssignedAssignments(params);

        int totalPages = Math.max(
                (int) Math.ceil(
                        (double) totalRecords / pageSize
                ),
                1
        );

        if(page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        return new AdminAssignedAssignmentPageResponse(
                assignedAssignmentService
                        .getAssignedAssignments(params)
                        .stream()
                        .map(adminAssignedAssignmentMapper::toResponse)
                        .toList(),

                page,
                totalPages,
                totalRecords
        );
    }

    @GetMapping("/available-assignments")
    public List<AdminAvailableAssignmentResponse> getAvailableAssignments(
            @RequestParam Integer studentId
    ) {
        List<Enrollment> enrollments =
                enrollmentService
                        .getActiveEnrollmentsByStudent(studentId);

        Map<Integer, AdminAvailableAssignmentResponse> result =
                new LinkedHashMap<>();

        for(Enrollment enrollment : enrollments) {
            Integer courseId =
                    enrollment
                            .getCourseClass()
                            .getCourse()
                            .getId();

            for(Assignment assignment :
                    assignmentService
                            .getAssignmentsByCourse(courseId)) {

                result.putIfAbsent(
                        assignment.getId(),
                        new AdminAvailableAssignmentResponse(
                                assignment.getId(),
                                assignment.getName(),
                                assignment.getCourse().getId(),
                                assignment.getCourse().getName()
                        )
                );
            }
        }

        return List.copyOf(result.values());
    }

    @PostMapping("/release-current")
    public AdminAssignedAssignmentActionResponse releaseCurrent(
            @RequestBody ReleaseCurrentAssignmentRequest request
    ) {
        if(request.studentLearningPathId() == null)
            throw new IllegalArgumentException(
                    "Vui lòng chọn tiến độ học!"
            );

        AssignedAssignment assigned =
                assignedAssignmentService
                        .assignCurrentDetail(
                                request.studentLearningPathId(),
                                request.availableAt(),
                                request.dueAt()
                        );

        return new AdminAssignedAssignmentActionResponse(
                assigned.getId(),
                "Phát bài theo lộ trình thành công!"
        );
    }

    @PostMapping("/manual")
    public AdminAssignedAssignmentActionResponse assignManual(
            @RequestBody ManualAssignmentRequest request,
            Authentication authentication
    ) {
        if(request.studentId() == null)
            throw new IllegalArgumentException(
                    "Vui lòng chọn học viên!"
            );

        if(request.assignmentId() == null)
            throw new IllegalArgumentException(
                    "Vui lòng chọn bài tập!"
            );

        User assignedBy =
                userService.getUserByUsername(
                        authentication.getName()
                );

        AssignedAssignment assigned =
                assignedAssignmentService.assignManual(
                        request.studentId(),
                        request.assignmentId(),
                        assignedBy,
                        request.availableAt(),
                        request.dueAt()
                );

        return new AdminAssignedAssignmentActionResponse(
                assigned.getId(),
                "Giao bài thủ công thành công!"
        );
    }

    @PatchMapping("/{id}/status")
    public AdminAssignedAssignmentActionResponse updateStatus(
            @PathVariable Integer id,
            @RequestBody UpdateAssignedAssignmentStatusRequest request
    ) {
        if(request.status() == null
                || request.status().isBlank())
            throw new IllegalArgumentException(
                    "Trạng thái không được để trống!"
            );

        AssignedAssignment.AssignedStatus status;

        try {
            status =
                    AssignedAssignment.AssignedStatus.valueOf(
                            request.status()
                                    .trim()
                                    .toUpperCase()
                    );
        } catch(IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Trạng thái bài đã giao không hợp lệ!"
            );
        }

        assignedAssignmentService
                .updateAvailabilityStatus(id, status);

        return new AdminAssignedAssignmentActionResponse(
                id,
                "Cập nhật trạng thái bài thành công!"
        );
    }
}