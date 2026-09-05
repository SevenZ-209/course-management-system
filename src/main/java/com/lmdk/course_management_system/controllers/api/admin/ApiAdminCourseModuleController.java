package com.lmdk.course_management_system.controllers.api.admin;

import com.lmdk.course_management_system.dto.admin.coursemodule.*;
import com.lmdk.course_management_system.mappers.admin.AdminCourseModuleMapper;
import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.pojo.CourseModule;
import com.lmdk.course_management_system.services.CourseModuleService;
import com.lmdk.course_management_system.services.CourseService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/course-modules")
@RequiredArgsConstructor
public class ApiAdminCourseModuleController {

    private final CourseModuleService moduleService;
    private final CourseService courseService;
    private final AdminCourseModuleMapper adminCourseModuleMapper;

    @Value("${modules.page-size:10}")
    private int pageSize;

    @GetMapping
    public AdminCourseModulePageResponse getModules(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) String status
    ) {
        page = Math.max(page, 1);

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));

        if(kw != null && !kw.isBlank())
            params.put("kw", kw.trim());

        if(courseId != null)
            params.put("courseId", String.valueOf(courseId));

        if(status != null && !status.isBlank())
            params.put("status", status);

        long totalRecords = moduleService.countModules(params);

        int totalPages = Math.max(
                (int) Math.ceil((double) totalRecords / pageSize),
                1
        );

        if(page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        return new AdminCourseModulePageResponse(
                moduleService.getModules(params)
                        .stream()
                        .map(adminCourseModuleMapper::toResponse)
                        .toList(),
                page,
                totalPages,
                totalRecords
        );
    }

    @GetMapping("/options")
    public List<AdminCourseModuleResponse> getModuleOptions(
            @RequestParam(required = false) Integer courseId
    ) {
        var modules = courseId == null
                ? moduleService.getAllModules()
                : moduleService.getModulesByCourse(courseId);

        return modules.stream()
                .map(adminCourseModuleMapper::toResponse)
                .toList();
    }

    @PostMapping
    public AdminCourseModuleActionResponse addModule(
            @RequestBody CourseModuleRequest request
    ) {
        Course course = requireCourse(request.courseId());

        CourseModule module = new CourseModule();
        module.setName(request.name());
        module.setCourse(course);
        module.setOrderNumber(request.orderNumber());

        moduleService.addModule(module);

        return new AdminCourseModuleActionResponse(
                module.getId(),
                "Thêm module thành công!"
        );
    }

    @PutMapping("/{moduleId}")
    public AdminCourseModuleActionResponse updateModule(
            @PathVariable Integer moduleId,
            @RequestBody CourseModuleRequest request
    ) {
        CourseModule module = requireModule(moduleId);
        Course course = requireCourse(request.courseId());

        module.setName(request.name());
        module.setCourse(course);
        module.setOrderNumber(request.orderNumber());

        moduleService.updateModule(module);

        return new AdminCourseModuleActionResponse(
                moduleId,
                "Cập nhật module thành công!"
        );
    }

    @PatchMapping("/{moduleId}/status")
    public AdminCourseModuleActionResponse updateStatus(
            @PathVariable Integer moduleId,
            @RequestBody UpdateCourseModuleStatusRequest request
    ) {
        CourseModule module = requireModule(moduleId);

        if(request.status() == null || request.status().isBlank())
            throw new IllegalArgumentException(
                    "Trạng thái không được để trống!"
            );

        try {
            module.setStatus(
                    CourseModule.ModuleStatus.valueOf(
                            request.status().trim().toUpperCase()
                    )
            );
        } catch(IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Trạng thái không hợp lệ!"
            );
        }

        moduleService.updateModule(module);

        return new AdminCourseModuleActionResponse(
                moduleId,
                "Cập nhật trạng thái thành công!"
        );
    }

    private CourseModule requireModule(Integer moduleId) {
        CourseModule module = moduleService.getModuleById(moduleId);

        if(module == null)
            throw new IllegalArgumentException(
                    "Không tìm thấy module!"
            );

        return module;
    }

    private Course requireCourse(Integer courseId) {
        if(courseId == null)
            throw new IllegalArgumentException(
                    "Vui lòng chọn khóa học!"
            );

        Course course = courseService.getCourseById(courseId);

        if(course == null)
            throw new IllegalArgumentException(
                    "Khóa học không tồn tại!"
            );

        return course;
    }
}