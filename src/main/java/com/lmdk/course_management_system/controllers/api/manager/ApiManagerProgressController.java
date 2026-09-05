package com.lmdk.course_management_system.controllers.api.manager;

import com.lmdk.course_management_system.dto.manager.progress.ManagerStudentProgressPageResponse;
import com.lmdk.course_management_system.dto.manager.progress.ManagerStudentProgressResponse;
import com.lmdk.course_management_system.mappers.manager.ManagerStudentProgressMapper;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.LearningPathDetail;
import com.lmdk.course_management_system.pojo.StudentLearningPath;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.LearningPathDetailService;
import com.lmdk.course_management_system.services.StudentLearningPathService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/manager/progress")
@RequiredArgsConstructor
public class ApiManagerProgressController {

    private final EnrollmentService enrollmentService;
    private final StudentLearningPathService studentLearningPathService;
    private final LearningPathDetailService learningPathDetailService;
    private final ManagerStudentProgressMapper progressMapper;

    @Value("${enrollments.page-size:10}")
    private int pageSize;

    @GetMapping
    public ManagerStudentProgressPageResponse getProgress(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer classId,
            @RequestParam(required = false) String status
    ) {
        page = Math.max(page, 1);
        String normalizedStatus = normalizeStatus(status);

        Map<String, String> baseParams = new HashMap<>();
        baseParams.put("status", Enrollment.EnrollmentStatus.ACTIVE.name());
        if(kw != null && !kw.isBlank()) baseParams.put("kw", kw.trim());
        if(courseId != null) baseParams.put("courseId", String.valueOf(courseId));
        if(classId != null) baseParams.put("classId", String.valueOf(classId));

        long inProgressCount = countByProgressStatus(baseParams, StudentLearningPath.ProgressStatus.IN_PROGRESS);
        long pausedCount = countByProgressStatus(baseParams, StudentLearningPath.ProgressStatus.PAUSED);
        long completedCount = countByProgressStatus(baseParams, StudentLearningPath.ProgressStatus.COMPLETED);
        long activeCount = enrollmentService.countEnrollments(baseParams);
        long noPathCount = Math.max(activeCount - inProgressCount - pausedCount - completedCount, 0L);

        Map<String, String> params = new HashMap<>(baseParams);
        params.put("page", String.valueOf(page));
        if(normalizedStatus != null) params.put("progressStatus", normalizedStatus);

        long totalRecords = enrollmentService.countEnrollments(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);
        if(page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        List<Enrollment> enrollments = enrollmentService.getEnrollments(params);
        List<Integer> studentIds = enrollments.stream()
                .map(enrollment -> enrollment.getStudent().getId()).distinct().toList();
        List<Integer> courseIds = enrollments.stream()
                .map(enrollment -> enrollment.getCourseClass().getCourse().getId()).distinct().toList();

        Map<String, StudentLearningPath> progressByStudentCourse = new HashMap<>();
        studentLearningPathService.getStudentLearningPathsByStudentsAndCourses(studentIds, courseIds)
                .forEach(progress -> progressByStudentCourse.putIfAbsent(
                        key(progress.getStudent().getId(), progress.getLearningPath().getCourse().getId()), progress
                ));

        List<Integer> learningPathIds = progressByStudentCourse.values().stream()
                .map(progress -> progress.getLearningPath().getId()).distinct().toList();
        Map<Integer, List<LearningPathDetail>> detailsByPath = learningPathDetailService
                .getDetailsByLearningPaths(learningPathIds)
                .stream()
                .collect(Collectors.groupingBy(detail -> detail.getLearningPath().getId()));

        List<ManagerStudentProgressResponse> progress = enrollments.stream()
                .map(enrollment -> {
                    Integer enrollmentCourseId = enrollment.getCourseClass().getCourse().getId();
                    StudentLearningPath studentProgress = progressByStudentCourse.get(
                            key(enrollment.getStudent().getId(), enrollmentCourseId)
                    );
                    List<LearningPathDetail> details = studentProgress == null
                            ? List.of()
                            : detailsByPath.getOrDefault(studentProgress.getLearningPath().getId(), List.of());
                    return progressMapper.toResponse(enrollment, studentProgress, details);
                })
                .toList();

        return new ManagerStudentProgressPageResponse(
                progress, page, totalPages, totalRecords,
                inProgressCount, pausedCount, completedCount, noPathCount
        );
    }

    private long countByProgressStatus(
            Map<String, String> baseParams,
            StudentLearningPath.ProgressStatus status
    ) {
        Map<String, String> params = new HashMap<>(baseParams);
        params.put("progressStatus", status.name());
        return enrollmentService.countEnrollments(params);
    }

    private String normalizeStatus(String status) {
        if(status == null || status.isBlank()) return null;
        try {
            return StudentLearningPath.ProgressStatus.valueOf(status.trim().toUpperCase()).name();
        } catch(IllegalArgumentException ex) {
            throw new IllegalArgumentException("Trạng thái tiến độ không hợp lệ!");
        }
    }

    private String key(Integer studentId, Integer courseId) {
        return studentId + ":" + courseId;
    }
}
