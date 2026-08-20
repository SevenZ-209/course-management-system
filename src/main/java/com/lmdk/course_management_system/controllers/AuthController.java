package com.lmdk.course_management_system.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AuthController {

    @GetMapping("/")
    public String home() {
        return "redirect:/admin/nguoi-dung";
    }

    @GetMapping("/admin/login")
    public String login() {
        return "login";
    }
}