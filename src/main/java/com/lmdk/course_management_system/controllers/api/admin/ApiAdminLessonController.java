package com.lmdk.course_management_system.controllers.api.admin;

import com.lmdk.course_management_system.dto.admin.lesson.AdminLessonActionResponse;
import com.lmdk.course_management_system.dto.admin.lesson.AdminLessonPageResponse;
import com.lmdk.course_management_system.dto.cloudinary.CloudinaryUploadResult;
import com.lmdk.course_management_system.mappers.admin.AdminLessonMapper;
import com.lmdk.course_management_system.pojo.CourseModule;
import com.lmdk.course_management_system.pojo.Lesson;
import com.lmdk.course_management_system.services.CloudinaryService;
import com.lmdk.course_management_system.services.CourseModuleService;
import com.lmdk.course_management_system.services.LessonService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/lessons")
@RequiredArgsConstructor
public class ApiAdminLessonController {

    private final LessonService lessonService;
    private final CourseModuleService moduleService;
    private final CloudinaryService cloudinaryService;
    private final AdminLessonMapper adminLessonMapper;

    @Value("${lessons.page-size:10}")
    private int pageSize;

    @GetMapping
    public AdminLessonPageResponse getLessons(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer moduleId
    ) {
        page = Math.max(page, 1);

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));

        if(kw != null && !kw.isBlank())
            params.put("kw", kw.trim());

        if(courseId != null)
            params.put("courseId", String.valueOf(courseId));

        if(moduleId != null)
            params.put("moduleId", String.valueOf(moduleId));

        long totalRecords = lessonService.countLessons(params);
        int totalPages = Math.max(
                (int) Math.ceil((double) totalRecords / pageSize),
                1
        );

        if(page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        return new AdminLessonPageResponse(
                lessonService.getLessons(params)
                        .stream()
                        .map(adminLessonMapper::toResponse)
                        .toList(),
                page,
                totalPages,
                totalRecords
        );
    }

    @PostMapping
    public AdminLessonActionResponse addLesson(
            @RequestParam String name,
            @RequestParam Integer moduleId,
            @RequestParam Integer orderNumber,
            @RequestParam MultipartFile file
    ) {
        CourseModule module = requireModule(moduleId);
        CloudinaryUploadResult uploadResult = null;

        try {
            uploadResult = cloudinaryService.uploadPdf(file);

            Lesson lesson = new Lesson();
            lesson.setName(name);
            lesson.setCourseModule(module);
            lesson.setOrderNumber(orderNumber);
            lesson.setFileUrl(uploadResult.url());
            lesson.setFilePublicId(uploadResult.publicId());
            lesson.setFileName(uploadResult.fileName());

            lessonService.addLesson(lesson);

            return new AdminLessonActionResponse(
                    lesson.getId(),
                    "Thêm bài học thành công!"
            );
        } catch(IllegalArgumentException ex) {
            if(uploadResult != null)
                cloudinaryService.deletePdf(uploadResult.publicId());

            throw ex;
        }
    }

    @PutMapping("/{lessonId}")
    public AdminLessonActionResponse updateLesson(
            @PathVariable Integer lessonId,
            @RequestParam String name,
            @RequestParam Integer moduleId,
            @RequestParam Integer orderNumber,
            @RequestParam(required = false) MultipartFile file
    ) {
        Lesson lesson = requireLesson(lessonId);
        CourseModule module = requireModule(moduleId);

        String oldPublicId = lesson.getFilePublicId();
        String oldFileUrl = lesson.getFileUrl();
        String oldFileName = lesson.getFileName();

        CloudinaryUploadResult newUpload = null;

        try {
            lesson.setName(name);
            lesson.setCourseModule(module);
            lesson.setOrderNumber(orderNumber);

            if(file != null && !file.isEmpty()) {
                newUpload = cloudinaryService.uploadPdf(file);

                lesson.setFilePublicId(newUpload.publicId());
                lesson.setFileUrl(newUpload.url());
                lesson.setFileName(newUpload.fileName());
            }

            lessonService.updateLesson(lesson);

            if(newUpload != null
                    && oldPublicId != null
                    && !oldPublicId.isBlank())
                cloudinaryService.deletePdf(oldPublicId);

            return new AdminLessonActionResponse(
                    lessonId,
                    "Cập nhật bài học thành công!"
            );
        } catch(IllegalArgumentException ex) {
            if(newUpload != null) {
                cloudinaryService.deletePdf(newUpload.publicId());
                lesson.setFilePublicId(oldPublicId);
                lesson.setFileUrl(oldFileUrl);
                lesson.setFileName(oldFileName);
            }

            throw ex;
        }
    }

    private Lesson requireLesson(Integer lessonId) {
        Lesson lesson = lessonService.getLessonById(lessonId);

        if(lesson == null)
            throw new IllegalArgumentException(
                    "Không tìm thấy bài học!"
            );

        return lesson;
    }

    private CourseModule requireModule(Integer moduleId) {
        if(moduleId == null)
            throw new IllegalArgumentException(
                    "Vui lòng chọn module!"
            );

        CourseModule module = moduleService.getModuleById(moduleId);

        if(module == null)
            throw new IllegalArgumentException(
                    "Module không tồn tại!"
            );

        return module;
    }
}