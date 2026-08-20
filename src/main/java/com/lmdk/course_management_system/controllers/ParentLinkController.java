package com.lmdk.course_management_system.controllers;

import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.ParentLinkService;
import com.lmdk.course_management_system.services.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequestMapping("/admin/parent-links")
@RequiredArgsConstructor
public class ParentLinkController {

    private final ParentLinkService parentLinkService;
    private final UserService userService;

    @Value("${parent-links.page-size:10}")
    private int pageSize;

    @GetMapping
    public String parentLinks(@RequestParam Map<String, String> params, Model model) {
        int page = parsePage(params.get("page"));
        params.put("page", String.valueOf(page));

        long totalRecords = parentLinkService.countParentLinks(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);

        if (page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        model.addAttribute("parentLinks", parentLinkService.getParentLinks(params));
        model.addAttribute("students", userService.getUsersByRole(User.UserRole.STUDENT));
        model.addAttribute("parents", userService.getUsersByRole(User.UserRole.PARENT));
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalRecords", totalRecords);
        model.addAttribute("kw", params.getOrDefault("kw", ""));
        model.addAttribute("studentId", params.getOrDefault("studentId", ""));
        model.addAttribute("parentId", params.getOrDefault("parentId", ""));
        model.addAttribute("status", params.getOrDefault("status", ""));
        model.addAttribute("now", LocalDateTime.now());

        return "admin/parent-links";
    }

    @PostMapping("/create")
    public String createLink(
            @RequestParam Integer studentId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime expiresAt,
            RedirectAttributes redirectAttributes) {

        User student = userService.getUserById(studentId);

        try {
            parentLinkService.createParentLink(student, expiresAt);
            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Tạo mã liên kết thành công!"
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }

        return "redirect:/admin/parent-links";
    }

    @PostMapping("/expire")
    public String expireLink(@RequestParam Integer linkId,
                             RedirectAttributes redirectAttributes) {
        try {
            parentLinkService.expireParentLink(linkId);

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Đã vô hiệu hóa mã liên kết!"
            );
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );
        }

        return "redirect:/admin/parent-links";
    }

    private int parsePage(String page) {
        try {
            return Math.max(Integer.parseInt(page), 1);
        } catch (Exception ex) {
            return 1;
        }
    }
}