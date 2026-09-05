package com.lmdk.course_management_system.controllers.api.admin;

import com.lmdk.course_management_system.dto.admin.assignment.*;
import com.lmdk.course_management_system.mappers.admin.AdminAssignmentMapper;
import com.lmdk.course_management_system.pojo.Assignment;
import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.pojo.Lesson;
import com.lmdk.course_management_system.services.AssignmentService;
import com.lmdk.course_management_system.services.CourseService;

import com.lmdk.course_management_system.services.LessonService;
import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/assignments")
@RequiredArgsConstructor
public class ApiAdminAssignmentController {

    private final AssignmentService assignmentService;
    private final CourseService courseService;
    private final AdminAssignmentMapper adminAssignmentMapper;
    private final LessonService lessonService;

    @Value("${assignments.page-size:10}")
    private int pageSize;

    @GetMapping
    public AdminAssignmentPageResponse getAssignments(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status
    ) {
        page = Math.max(page, 1);

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));

        if (kw != null && !kw.isBlank())
            params.put("kw", kw.trim());

        if (courseId != null)
            params.put("courseId", String.valueOf(courseId));

        if (type != null && !type.isBlank())
            params.put("type", type);

        if (status != null && !status.isBlank())
            params.put("status", status);

        long totalRecords =
                assignmentService.countAssignments(params);

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

        return new AdminAssignmentPageResponse(
                assignmentService
                        .getAssignments(params)
                        .stream()
                        .map(adminAssignmentMapper::toResponse)
                        .toList(),

                page,
                totalPages,
                totalRecords
        );
    }

    @GetMapping("/options")
    public List<AdminAssignmentOptionResponse> getOptions(
            @RequestParam(required = false) Integer courseId
    ) {

        List<Assignment> assignments =
                courseId == null
                        ? assignmentService.getAllAssignments()
                        : assignmentService.getAssignmentsByCourse(courseId);

        return assignments.stream()
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
    public AdminAssignmentActionResponse addAssignment(
            @RequestBody CreateAssignmentRequest request
    ) {
        if(request.courseId() == null)
            throw new IllegalArgumentException(
                    "Vui lòng chọn khóa học!"
            );

        Course course =
                courseService.getCourseById(
                        request.courseId()
                );

        if (course == null)
            throw new IllegalArgumentException(
                    "Khóa học không tồn tại!"
            );

        Assignment.AssignmentType type =
                parseType(request.type());

        Assignment assignment =
                new Assignment();

        assignment.setName(request.name());
        assignment.setCourse(course);
        assignment.setType(type);
        assignment.setMaximumScore(
                request.maximumScore()
        );
        assignment.setDurationMinutes(
                request.durationMinutes()
        );
        assignment.setStatus(
                Assignment.AssignmentStatus.ACTIVE
        );

        Lesson lesson =
                lessonService.getLessonById(
                        request.lessonId()
                );

        if (lesson == null)
            throw new IllegalArgumentException(
                    "Bài học không tồn tại!"
            );

        assignment.setLesson(lesson);

        assignmentService.addAssignment(assignment);

        return new AdminAssignmentActionResponse(
                assignment.getId(),
                "Thêm bài tập thành công!"
        );
    }

    @PutMapping("/{assignmentId}")
    public AdminAssignmentActionResponse updateAssignment(
            @PathVariable Integer assignmentId,
            @RequestBody UpdateAssignmentRequest request
    ) {
        Assignment assignment =
                requireAssignment(assignmentId);

        assignment.setName(request.name());
        assignment.setType(
                parseType(request.type())
        );
        assignment.setMaximumScore(
                request.maximumScore()
        );
        assignment.setDurationMinutes(
                request.durationMinutes()
        );

        assignmentService.updateAssignment(assignment);

        return new AdminAssignmentActionResponse(
                assignmentId,
                "Cập nhật bài tập thành công!"
        );
    }

    @PatchMapping("/{assignmentId}/status")
    public AdminAssignmentActionResponse updateStatus(
            @PathVariable Integer assignmentId,
            @RequestBody UpdateAssignmentStatusRequest request
    ) {
        Assignment assignment =
                requireAssignment(assignmentId);

        if (request.status() == null
                || request.status().isBlank())
            throw new IllegalArgumentException(
                    "Trạng thái không được để trống!"
            );

        try {
            assignment.setStatus(
                    Assignment.AssignmentStatus.valueOf(
                            request.status()
                                    .trim()
                                    .toUpperCase()
                    )
            );
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Trạng thái bài tập không hợp lệ!"
            );
        }

        assignmentService.updateAssignment(assignment);

        return new AdminAssignmentActionResponse(
                assignmentId,
                "Cập nhật trạng thái thành công!"
        );
    }

    private Assignment requireAssignment(
            Integer assignmentId
    ) {
        Assignment assignment =
                assignmentService
                        .getAssignmentById(assignmentId);

        if (assignment == null)
            throw new IllegalArgumentException(
                    "Không tìm thấy bài tập!"
            );

        return assignment;
    }

    private Assignment.AssignmentType parseType(
            String type
    ) {
        if (type == null || type.isBlank())
            throw new IllegalArgumentException(
                    "Loại bài tập không được để trống!"
            );

        try {
            return Assignment.AssignmentType.valueOf(
                    type.trim().toUpperCase()
            );
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Loại bài tập không hợp lệ!"
            );
        }
    }
}