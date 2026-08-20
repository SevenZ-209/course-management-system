package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.dto.cloudinary.CloudinaryUploadResult;
import com.lmdk.course_management_system.pojo.CourseModule;
import com.lmdk.course_management_system.pojo.Lesson;
import com.lmdk.course_management_system.services.CloudinaryService;
import com.lmdk.course_management_system.services.CourseModuleService;
import com.lmdk.course_management_system.services.CourseService;
import com.lmdk.course_management_system.services.LessonService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Map;

@Controller
@RequestMapping("/admin/lessons")
@RequiredArgsConstructor
public class LessonController {

    private final LessonService lessonService;
    private final CourseModuleService moduleService;
    private final CourseService courseService;
    private final CloudinaryService cloudinaryService;

    @Value("${lessons.page-size:10}")
    private int pageSize;

    @GetMapping
    public String lessons(@RequestParam Map<String, String> params, Model model) {
        int page = parsePage(params.get("page"));
        params.put("page", String.valueOf(page));

        long totalRecords = lessonService.countLessons(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        model.addAttribute("lessons", lessonService.getLessons(params));
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("modules", moduleService.getAllModules());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("kw", params.getOrDefault("kw", ""));
        model.addAttribute("courseId", params.getOrDefault("courseId", ""));
        model.addAttribute("moduleId", params.getOrDefault("moduleId", ""));

        return "admin/lessons";
    }

    @PostMapping("/add")
    public String addLesson(
            @RequestParam String name,
            @RequestParam Integer moduleId,
            @RequestParam Integer orderNumber,
            @RequestParam MultipartFile file,
            RedirectAttributes redirectAttributes
    ) {

        CourseModule module =
                moduleService.getModuleById(moduleId);

        if(module == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Module không tồn tại!"
            );

            return "redirect:/admin/lessons";
        }

        CloudinaryUploadResult uploadResult = null;

        try {

            uploadResult =
                    cloudinaryService.uploadPdf(file);

            Lesson lesson = new Lesson();

            lesson.setName(name);
            lesson.setCourseModule(module);
            lesson.setOrderNumber(orderNumber);

            lesson.setFileUrl(
                    uploadResult.url()
            );

            lesson.setFilePublicId(
                    uploadResult.publicId()
            );

            lesson.setFileName(
                    uploadResult.fileName()
            );

            lessonService.addLesson(lesson);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Thêm bài học thành công!"
            );

        } catch (IllegalArgumentException ex) {

            if(uploadResult != null) {
                cloudinaryService.deletePdf(
                        uploadResult.publicId()
                );
            }

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }

        return "redirect:/admin/lessons";
    }

    @PostMapping("/update")
    public String updateLesson(
            @RequestParam Integer lessonId,
            @RequestParam String name,
            @RequestParam Integer moduleId,
            @RequestParam Integer orderNumber,
            @RequestParam(required = false) MultipartFile file,
            RedirectAttributes redirectAttributes
    ) {

        Lesson lesson =
                lessonService.getLessonById(lessonId);

        CourseModule module =
                moduleService.getModuleById(moduleId);

        if(lesson == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Không tìm thấy bài học!"
            );

            return "redirect:/admin/lessons";
        }

        if(module == null) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Module không tồn tại!"
            );

            return "redirect:/admin/lessons";
        }

        String oldPublicId =
                lesson.getFilePublicId();

        String oldFileUrl =
                lesson.getFileUrl();

        String oldFileName =
                lesson.getFileName();

        CloudinaryUploadResult newUpload = null;

        try {

            lesson.setName(name);
            lesson.setCourseModule(module);
            lesson.setOrderNumber(orderNumber);

            if(file != null && !file.isEmpty()) {

                newUpload =
                        cloudinaryService.uploadPdf(file);

                lesson.setFilePublicId(
                        newUpload.publicId()
                );

                lesson.setFileUrl(
                        newUpload.url()
                );

                lesson.setFileName(
                        newUpload.fileName()
                );
            }

            lessonService.updateLesson(lesson);

            if(newUpload != null
                    && oldPublicId != null
                    && !oldPublicId.isBlank()) {

                cloudinaryService.deletePdf(
                        oldPublicId
                );
            }

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Cập nhật bài học thành công!"
            );

        } catch (IllegalArgumentException ex) {

            if(newUpload != null) {

                cloudinaryService.deletePdf(
                        newUpload.publicId()
                );

                lesson.setFilePublicId(oldPublicId);
                lesson.setFileUrl(oldFileUrl);
                lesson.setFileName(oldFileName);
            }

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }

        return "redirect:/admin/lessons";
    }

    private int parsePage(String page) {
        try {
            return Math.max(Integer.parseInt(page), 1);
        } catch (Exception ex) {
            return 1;
        }
    }
}