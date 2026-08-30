package com.lmdk.course_management_system.controllers.api.publicapi;

import com.lmdk.course_management_system.dto.catalog.course.*;
import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.services.CourseClassService;
import com.lmdk.course_management_system.services.CourseService;
import com.lmdk.course_management_system.services.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class ApiPublicCourseController {

    private final CourseClassService classService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    @Value("${courses.page-size:10}")
    private int pageSize;

    @GetMapping
    public PublicCoursePageResponse getCourses(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String sort
    ) {
        page = Math.max(page, 1);

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        params.put("status", Course.CourseStatus.ACTIVE.name());

        if(minPrice != null)
            params.put("minPrice", minPrice.toString());

        if(maxPrice != null)
            params.put("maxPrice", maxPrice.toString());

        if(sort != null && !sort.isBlank())
            params.put("sort", sort);

        if(kw != null && !kw.isBlank())
            params.put("kw", kw.trim());

        if(categoryId != null)
            params.put("categoryId", String.valueOf(categoryId));

        long totalRecords = courseService.countCourses(params);
        int totalPages = Math.max(
                (int) Math.ceil((double) totalRecords / pageSize),
                1
        );

        if(page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        var courses = courseService.getCourses(params)
                .stream()
                .map(this::toResponse)
                .toList();

        return new PublicCoursePageResponse(
                courses,
                page,
                totalPages,
                totalRecords
        );
    }

    @GetMapping("/{courseId}/classes")
    public List<PublicCourseClassResponse> getClasses(
            @PathVariable Integer courseId
    ) {
        Course course = courseService.getCourseById(courseId);

        if(course == null
                || course.getStatus() != Course.CourseStatus.ACTIVE)
            throw new IllegalArgumentException(
                    "Khóa học không tồn tại!"
            );

        return classService.getAllClasses()
                .stream()
                .filter(c ->
                        c.getCourse() != null
                                && c.getCourse().getId().equals(courseId)
                )
                .filter(c ->
                        c.getStatus() == CourseClass.ClassStatus.UPCOMING
                                || c.getStatus() == CourseClass.ClassStatus.ACTIVE
                )
                .map(c -> new PublicCourseClassResponse(
                        c.getId(),
                        c.getName(),
                        c.getStartDate(),
                        c.getEndDate(),
                        enrollmentService.countOccupiedByClass(c.getId()),
                        c.getMaxStudents(),
                        c.getStatus().name()
                ))
                .toList();
    }

    @GetMapping("/{courseId}")
    public PublicCourseResponse getCourse(
            @PathVariable Integer courseId
    ) {
        Course course = courseService.getCourseById(courseId);

        if(course == null
                || course.getStatus() != Course.CourseStatus.ACTIVE)
            throw new IllegalArgumentException(
                    "Khóa học không tồn tại!"
            );

        return toResponse(course);
    }

    private PublicCourseResponse toResponse(Course course) {
        return new PublicCourseResponse(
                course.getId(),
                course.getName(),
                course.getDescription(),
                course.getTuitionFee(),
                course.getImageUrl(),
                course.getCategory().getId(),
                course.getCategory().getName()
        );
    }
}