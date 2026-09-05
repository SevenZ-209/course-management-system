package com.lmdk.course_management_system.controllers.api.admin;

import com.lmdk.course_management_system.dto.admin.learningpath.*;
import com.lmdk.course_management_system.mappers.admin.AdminLearningPathMapper;
import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.pojo.LearningPath;
import com.lmdk.course_management_system.services.CourseService;
import com.lmdk.course_management_system.services.LearningPathService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/learning-paths")
@RequiredArgsConstructor
public class ApiAdminLearningPathController {

    private final LearningPathService learningPathService;
    private final CourseService courseService;
    private final AdminLearningPathMapper adminLearningPathMapper;

    @Value("${learning-paths.page-size:10}")
    private int pageSize;

    @GetMapping
    public AdminLearningPathPageResponse getLearningPaths(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) String status
    ) {
        page = Math.max(page, 1);

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));

        if (kw != null && !kw.isBlank())
            params.put("kw", kw.trim());

        if (courseId != null)
            params.put("courseId", String.valueOf(courseId));

        if (status != null && !status.isBlank())
            params.put("status", status);

        long totalRecords =
                learningPathService.countLearningPaths(params);

        int totalPages = Math.max(
                (int) Math.ceil((double) totalRecords / pageSize),
                1
        );

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        return new AdminLearningPathPageResponse(
                learningPathService
                        .getLearningPaths(params)
                        .stream()
                        .map(adminLearningPathMapper::toResponse)
                        .toList(),
                page,
                totalPages,
                totalRecords
        );
    }

    @GetMapping("/options")
    public List<AdminLearningPathResponse> getOptions(
            @RequestParam(required = false) Integer courseId
    ) {
        var paths = courseId == null
                ? learningPathService.getAllLearningPaths()
                : learningPathService.getLearningPathsByCourse(courseId);

        return paths.stream()
                .map(adminLearningPathMapper::toResponse)
                .toList();
    }

    @PostMapping
    public AdminLearningPathActionResponse addLearningPath(
            @RequestBody CreateLearningPathRequest request
    ) {
        Course course =
                courseService.getCourseById(request.courseId());

        if (course == null)
            throw new IllegalArgumentException(
                    "Khóa học không tồn tại!"
            );

        LearningPath learningPath = new LearningPath();

        learningPath.setName(request.name());
        learningPath.setCourse(course);
        learningPath.setAssignmentsPerDay(
                request.assignmentsPerDay()
        );
        learningPath.setStatus(
                LearningPath.LearningPathStatus.ACTIVE
        );

        learningPathService.addLearningPath(learningPath);

        return new AdminLearningPathActionResponse(
                learningPath.getId(),
                "Thêm lộ trình thành công!"
        );
    }

    @PutMapping("/{learningPathId}")
    public AdminLearningPathActionResponse updateLearningPath(
            @PathVariable Integer learningPathId,
            @RequestBody UpdateLearningPathRequest request
    ) {
        LearningPath learningPath =
                requireLearningPath(learningPathId);

        learningPath.setName(request.name());
        learningPath.setAssignmentsPerDay(
                request.assignmentsPerDay()
        );

        learningPathService.updateLearningPath(learningPath);

        return new AdminLearningPathActionResponse(
                learningPathId,
                "Cập nhật lộ trình thành công!"
        );
    }

    @PatchMapping("/{learningPathId}/status")
    public AdminLearningPathActionResponse updateStatus(
            @PathVariable Integer learningPathId,
            @RequestBody UpdateLearningPathStatusRequest request
    ) {
        LearningPath learningPath =
                requireLearningPath(learningPathId);

        if (request.status() == null
                || request.status().isBlank())
            throw new IllegalArgumentException(
                    "Trạng thái không được để trống!"
            );

        try {
            learningPath.setStatus(
                    LearningPath.LearningPathStatus.valueOf(
                            request.status()
                                    .trim()
                                    .toUpperCase()
                    )
            );
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Trạng thái lộ trình không hợp lệ!"
            );
        }

        learningPathService.updateLearningPath(learningPath);

        return new AdminLearningPathActionResponse(
                learningPathId,
                "Cập nhật trạng thái thành công!"
        );
    }

    private LearningPath requireLearningPath(
            Integer learningPathId
    ) {
        LearningPath learningPath =
                learningPathService
                        .getLearningPathById(learningPathId);

        if (learningPath == null)
            throw new IllegalArgumentException(
                    "Không tìm thấy lộ trình!"
            );

        return learningPath;
    }
}