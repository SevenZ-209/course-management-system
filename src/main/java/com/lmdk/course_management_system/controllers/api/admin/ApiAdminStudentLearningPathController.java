package com.lmdk.course_management_system.controllers.api.admin;

import com.lmdk.course_management_system.dto.admin.learningpath.*;
import com.lmdk.course_management_system.mappers.admin.AdminStudentLearningPathMapper;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.LearningPath;
import com.lmdk.course_management_system.pojo.StudentLearningPath;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.LearningPathService;
import com.lmdk.course_management_system.services.StudentLearningPathService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/student-learning-paths")
@RequiredArgsConstructor
public class ApiAdminStudentLearningPathController {

    private final StudentLearningPathService studentLearningPathService;
    private final LearningPathService learningPathService;
    private final EnrollmentService enrollmentService;
    private final AdminStudentLearningPathMapper adminStudentLearningPathMapper;

    @Value("${student-learning-paths.page-size:10}")
    private int pageSize;

    @GetMapping
    public AdminStudentLearningPathPageResponse getStudentLearningPaths(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer learningPathId,
            @RequestParam(required = false) String status
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

        if (status != null && !status.isBlank())
            params.put("status", status);

        long totalRecords =
                studentLearningPathService
                        .countStudentLearningPaths(params);

        int totalPages = Math.max(
                (int) Math.ceil(
                        (double) totalRecords / pageSize
                ),
                1
        );

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        return new AdminStudentLearningPathPageResponse(
                studentLearningPathService
                        .getStudentLearningPaths(params)
                        .stream()
                        .map(adminStudentLearningPathMapper::toResponse)
                        .toList(),

                page,
                totalPages,
                totalRecords
        );
    }

    @GetMapping("/available-paths")
    public List<AdminAvailableLearningPathResponse> getAvailablePaths(
            @RequestParam Integer studentId
    ) {
        List<Enrollment> enrollments =
                enrollmentService
                        .getActiveEnrollmentsByStudent(studentId);

        Map<Integer, AdminAvailableLearningPathResponse> result =
                new LinkedHashMap<>();

        for (Enrollment enrollment : enrollments) {

            Integer courseId =
                    enrollment
                            .getCourseClass()
                            .getCourse()
                            .getId();

            for (LearningPath path :
                    learningPathService
                            .getLearningPathsByCourse(courseId)) {

                if (studentLearningPathService
                        .getStudentLearningPath(
                                studentId,
                                path.getId()
                        ) != null)
                    continue;

                result.putIfAbsent(
                        path.getId(),
                        new AdminAvailableLearningPathResponse(
                                path.getId(),
                                path.getName(),
                                path.getCourse().getId(),
                                path.getCourse().getName()
                        )
                );
            }
        }

        return List.copyOf(result.values());
    }

    @PostMapping("/assign")
    public AdminStudentLearningPathActionResponse assignLearningPath(
            @RequestBody AssignLearningPathRequest request
    ) {
        StudentLearningPath studentLearningPath =
                studentLearningPathService
                        .assignLearningPath(
                                request.studentId(),
                                request.learningPathId()
                        );

        return new AdminStudentLearningPathActionResponse(
                studentLearningPath.getId(),
                "Gán lộ trình cho học viên thành công!"
        );
    }

    @PatchMapping("/{id}/pause")
    public AdminStudentLearningPathActionResponse pauseLearningPath(
            @PathVariable Integer id
    ) {
        studentLearningPathService
                .pauseLearningPath(id);

        return new AdminStudentLearningPathActionResponse(
                id,
                "Đã tạm dừng lộ trình!"
        );
    }

    @PatchMapping("/{id}/resume")
    public AdminStudentLearningPathActionResponse resumeLearningPath(
            @PathVariable Integer id
    ) {
        studentLearningPathService
                .resumeLearningPath(id);

        return new AdminStudentLearningPathActionResponse(
                id,
                "Đã tiếp tục lộ trình!"
        );
    }

    @GetMapping("/in-progress-options")
    public List<AdminStudentLearningPathResponse> getInProgressOptions(
            @RequestParam(required = false) Integer studentId
    ) {
        if (studentId == null)
            return List.of();

        return studentLearningPathService.getStudentLearningPathsByStudent(studentId).stream()
                .filter(progress -> progress.getStatus() == StudentLearningPath.ProgressStatus.IN_PROGRESS)
                .filter(progress -> progress.getCurrentDetail() != null
                        && progress.getCurrentDetail().getAssignment() != null)
                .map(adminStudentLearningPathMapper::toResponse)
                .toList();
    }
}