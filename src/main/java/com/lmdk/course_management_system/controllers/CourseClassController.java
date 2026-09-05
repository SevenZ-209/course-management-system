package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.CourseClassService;
import com.lmdk.course_management_system.services.CourseService;
import com.lmdk.course_management_system.services.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/classes")
@RequiredArgsConstructor
public class CourseClassController {

    private final CourseClassService classService;
    private final CourseService courseService;
    private final UserService userService;

    @Value("${classes.page-size:10}")
    private int pageSize;

    @GetMapping
    public String classes(@RequestParam Map<String, String> params, Model model) {
        int page = parsePage(params.get("page"));
        params.put("page", String.valueOf(page));

        long totalRecords = classService.countClasses(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        model.addAttribute("classes", classService.getClasses(params));
        model.addAttribute("courses", courseService.getAllCourses());
        model.addAttribute("selectedTeacher", selectedTeacher(params.get("teacherId")));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("kw", params.getOrDefault("kw", ""));
        model.addAttribute("courseId", params.getOrDefault("courseId", ""));
        model.addAttribute("teacherId", params.getOrDefault("teacherId", ""));
        model.addAttribute("status", params.getOrDefault("status", ""));
        model.addAttribute("today", LocalDate.now());

        return "admin/classes";
    }

    @GetMapping("/teachers")
    @ResponseBody
    public List<Map<String, Object>> searchTeachers(@RequestParam String q) {
        if (q == null || q.trim().length() < 2)
            return List.of();

        return userService.searchUsersByRole(User.UserRole.TEACHER, q.trim(), 1, 20).stream()
                .filter(user -> user.getStatus() == User.UserStatus.ACTIVE)
                .map(user -> Map.<String, Object>of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "fullName", user.getFullName()
                ))
                .toList();
    }

    @PostMapping("/add")
    public String addClass(@RequestParam String name,
                           @RequestParam Integer courseId,
                           @RequestParam(required = false) Integer teacherId,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                           @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                           @RequestParam Integer maxStudents,
                           RedirectAttributes redirectAttributes) {
        Course course = courseService.getCourseById(courseId);
        User teacher = teacherId != null ? userService.getUserById(teacherId) : null;

        if (course == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khóa học không tồn tại!");
            return "redirect:/admin/classes";
        }

        if (!validTeacher(teacherId, teacher)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giáo viên không hợp lệ!");
            return "redirect:/admin/classes";
        }

        try {
            CourseClass courseClass = new CourseClass();
            courseClass.setName(name);
            courseClass.setCourse(course);
            courseClass.setTeacher(teacher);
            courseClass.setStartDate(startDate);
            courseClass.setEndDate(endDate);
            courseClass.setMaxStudents(maxStudents);

            classService.addClass(courseClass);
            redirectAttributes.addFlashAttribute("successMessage", "Thêm lớp học thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/classes";
    }

    @PostMapping("/update")
    public String updateClass(@RequestParam Integer classId,
                              @RequestParam String name,
                              @RequestParam Integer courseId,
                              @RequestParam(required = false) Integer teacherId,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                              @RequestParam Integer maxStudents,
                              RedirectAttributes redirectAttributes) {
        CourseClass courseClass = classService.getClassById(classId);
        Course course = courseService.getCourseById(courseId);
        User teacher = teacherId != null ? userService.getUserById(teacherId) : null;

        if (courseClass == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy lớp học!");
            return "redirect:/admin/classes";
        }

        if (course == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Khóa học không tồn tại!");
            return "redirect:/admin/classes";
        }

        if (!validTeacher(teacherId, teacher)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Giáo viên không hợp lệ!");
            return "redirect:/admin/classes";
        }

        try {
            courseClass.setName(name);
            courseClass.setCourse(course);
            courseClass.setTeacher(teacher);
            courseClass.setStartDate(startDate);
            courseClass.setEndDate(endDate);
            courseClass.setMaxStudents(maxStudents);

            classService.updateClass(courseClass);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật lớp học thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin/classes";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam Integer classId,
                               @RequestParam String status,
                               RedirectAttributes redirectAttributes) {
        CourseClass courseClass = classService.getClassById(classId);

        if (courseClass == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy lớp học!");
            return "redirect:/admin/classes";
        }

        if (!CourseClass.ClassStatus.CANCELED.name().equalsIgnoreCase(status)) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Trạng thái Sắp mở / Đang học / Hoàn thành được tự động theo thời gian lớp học!"
            );
            return "redirect:/admin/classes";
        }

        courseClass.setStatus(CourseClass.ClassStatus.CANCELED);
        classService.updateClass(courseClass);
        redirectAttributes.addFlashAttribute("successMessage", "Hủy lớp học thành công!");

        return "redirect:/admin/classes";
    }

    private User selectedTeacher(String teacherId) {
        try {
            if (teacherId == null || teacherId.isBlank())
                return null;
            User teacher = userService.getUserById(Integer.valueOf(teacherId));
            return validTeacher(Integer.valueOf(teacherId), teacher) ? teacher : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean validTeacher(Integer teacherId, User teacher) {
        if (teacherId == null)
            return true;

        return teacher != null
                && teacher.getRole() == User.UserRole.TEACHER
                && teacher.getStatus() == User.UserStatus.ACTIVE;
    }

    private int parsePage(String page) {
        try {
            return Math.max(Integer.parseInt(page), 1);
        } catch (Exception ex) {
            return 1;
        }
    }
}