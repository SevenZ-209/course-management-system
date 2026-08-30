package com.lmdk.course_management_system.controllers.api.admin;

import com.lmdk.course_management_system.dto.admin.course.*;
import com.lmdk.course_management_system.mappers.admin.AdminCourseMapper;
import com.lmdk.course_management_system.pojo.Category;
import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.services.CategoryService;
import com.lmdk.course_management_system.services.CourseService;
import com.lmdk.course_management_system.dto.cloudinary.CloudinaryUploadResult;
import com.lmdk.course_management_system.services.CloudinaryService;
import org.springframework.http.MediaType;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/courses")
@RequiredArgsConstructor
public class ApiAdminCourseController {

    private final CourseService courseService;
    private final CategoryService categoryService;
    private final AdminCourseMapper adminCourseMapper;
    private final CloudinaryService cloudinaryService;

    @Value("${courses.page-size:10}")
    private int pageSize;

    @GetMapping
    public AdminCoursePageResponse getCourses(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) Integer categoryId,
            @RequestParam(required = false) String status
    ) {
        page = Math.max(page, 1);

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));

        if(kw != null && !kw.isBlank())
            params.put("kw", kw.trim());

        if(categoryId != null)
            params.put("categoryId", String.valueOf(categoryId));

        if(status != null && !status.isBlank())
            params.put("status", status);

        long totalRecords =
                courseService.countCourses(params);

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

        return new AdminCoursePageResponse(
                courseService.getCourses(params)
                        .stream()
                        .map(adminCourseMapper::toResponse)
                        .toList(),
                page,
                totalPages,
                totalRecords
        );
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdminCourseActionResponse addCourse(
            @ModelAttribute CourseRequest request
    ) {
        Category category = requireCategory(request.categoryId());
        CloudinaryUploadResult upload = null;

        try {
            Course course = new Course();
            course.setName(request.name());
            course.setDescription(request.description());
            course.setTuitionFee(request.tuitionFee());
            course.setCategory(category);

            if(request.image() != null && !request.image().isEmpty()) {
                upload = cloudinaryService.uploadCourseImage(request.image());
                course.setImageUrl(upload.url());
                course.setImagePublicId(upload.publicId());
            }

            courseService.addCourse(course);

            return new AdminCourseActionResponse(
                    course.getId(),
                    "Thêm khóa học thành công!"
            );
        } catch(RuntimeException ex) {
            if(upload != null)
                cloudinaryService.deleteCourseImage(upload.publicId());

            throw ex;
        }
    }

    @PutMapping(
            value = "/{courseId}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    public AdminCourseActionResponse updateCourse(
            @PathVariable Integer courseId,
            @ModelAttribute CourseRequest request
    ) {
        Course course = requireCourse(courseId);
        Category category = requireCategory(request.categoryId());

        String oldPublicId = course.getImagePublicId();
        String oldImageUrl = course.getImageUrl();
        CloudinaryUploadResult newUpload = null;

        try {
            course.setName(request.name());
            course.setDescription(request.description());
            course.setTuitionFee(request.tuitionFee());
            course.setCategory(category);

            if(request.image() != null && !request.image().isEmpty()) {
                newUpload = cloudinaryService.uploadCourseImage(request.image());
                course.setImageUrl(newUpload.url());
                course.setImagePublicId(newUpload.publicId());
            }

            courseService.updateCourse(course);

            if(newUpload != null && oldPublicId != null && !oldPublicId.isBlank())
                cloudinaryService.deleteCourseImage(oldPublicId);

            return new AdminCourseActionResponse(
                    courseId,
                    "Cập nhật khóa học thành công!"
            );
        } catch(RuntimeException ex) {
            if(newUpload != null) {
                cloudinaryService.deleteCourseImage(newUpload.publicId());
                course.setImageUrl(oldImageUrl);
                course.setImagePublicId(oldPublicId);
            }

            throw ex;
        }
    }

    @GetMapping("/options")
    public List<AdminCourseResponse> getCourseOptions() {
        return courseService
                .getAllCourses()
                .stream()
                .map(adminCourseMapper::toResponse)
                .toList();
    }

    @PatchMapping("/{courseId}/status")
    public AdminCourseActionResponse updateStatus(
            @PathVariable Integer courseId,
            @RequestBody UpdateCourseStatusRequest request
    ) {
        Course course =
                requireCourse(courseId);

        if(request.status() == null
                || request.status().isBlank())
            throw new IllegalArgumentException(
                    "Trạng thái không được để trống!"
            );

        try {
            course.setStatus(
                    Course.CourseStatus.valueOf(
                            request.status()
                                    .trim()
                                    .toUpperCase()
                    )
            );
        } catch(IllegalArgumentException ex) {
            throw new IllegalArgumentException(
                    "Trạng thái không hợp lệ!"
            );
        }

        courseService.updateCourse(course);

        return new AdminCourseActionResponse(
                courseId,
                "Cập nhật trạng thái thành công!"
        );
    }

    private Course requireCourse(Integer courseId) {
        Course course =
                courseService.getCourseById(courseId);

        if(course == null)
            throw new IllegalArgumentException(
                    "Không tìm thấy khóa học!"
            );

        return course;
    }


    private Category requireCategory(Integer categoryId) {
        if(categoryId == null)
            throw new IllegalArgumentException(
                    "Vui lòng chọn danh mục!"
            );

        Category category =
                categoryService.getCategoryById(categoryId);

        if(category == null)
            throw new IllegalArgumentException(
                    "Danh mục không tồn tại!"
            );

        return category;
    }
}