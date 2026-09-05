package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Value("${users.page-size:10}")
    private int pageSize;

    @GetMapping
    public String users(@RequestParam Map<String, String> params, Model model) {
        int page = parsePage(params.get("page"));
        params.put("page", String.valueOf(page));

        long totalRecords = userService.countUsers(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        model.addAttribute("users", userService.getUsers(params));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRecords", totalRecords);

        model.addAttribute("kw", params.getOrDefault("kw", ""));
        model.addAttribute("role", params.getOrDefault("role", ""));
        model.addAttribute("status", params.getOrDefault("status", ""));

        return "admin/users";
    }

    @GetMapping("/student-options")
    @ResponseBody
    public List<Map<String, Object>> getStudentOptions(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return getUserOptions(User.UserRole.STUDENT, q, page, size);
    }

    @GetMapping("/parent-options")
    @ResponseBody
    public List<Map<String, Object>> getParentOptions(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        return getUserOptions(User.UserRole.PARENT, q, page, size);
    }

    @PostMapping("/update-role")
    public String updateRole(@RequestParam Integer userId,
                             @RequestParam String role,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        User user = userService.getUserById(userId);

        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng!");
            return "redirect:/admin/users";
        }

        if (user.getUsername().equals(authentication.getName())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không thể tự thay đổi quyền của mình!");
            return "redirect:/admin/users";
        }

        try {
            user.setRole(User.UserRole.valueOf(role));
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật quyền thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Quyền không hợp lệ!");
        }

        return "redirect:/admin/users";
    }

    @PostMapping("/update-status")
    public String updateStatus(@RequestParam Integer userId,
                               @RequestParam String status,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        User user = userService.getUserById(userId);

        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy người dùng!");
            return "redirect:/admin/users";
        }

        if (user.getUsername().equals(authentication.getName())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn không thể tự thay đổi trạng thái của mình!");
            return "redirect:/admin/users";
        }

        try {
            user.setStatus(User.UserStatus.valueOf(status));
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "Cập nhật trạng thái thành công!");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Trạng thái không hợp lệ!");
        }

        return "redirect:/admin/users";
    }

    private List<Map<String, Object>> getUserOptions(User.UserRole role, String q, Integer page, Integer size) {
        int safePage = page == null ? 1 : Math.max(page, 1);
        int safeSize = size == null ? 20 : Math.min(Math.max(size, 1), 50);
        String keyword = q == null ? "" : q.trim();

        return userService.searchUsersByRole(role, keyword, safePage, safeSize).stream()
                .map(user -> Map.<String, Object>of(
                        "id", user.getId(),
                        "username", user.getUsername(),
                        "fullName", user.getFullName()
                ))
                .toList();
    }

    private int parsePage(String page) {
        try {
            return Math.max(Integer.parseInt(page), 1);
        } catch (Exception ex) {
            return 1;
        }
    }
}