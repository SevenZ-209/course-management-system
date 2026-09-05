package com.lmdk.course_management_system.security;

import com.lmdk.course_management_system.configs.SpringSecurityConfigs;
import com.lmdk.course_management_system.filters.JwtFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SecurityAuthorizationMockMvcTest.SecurityProbeController.class)
@Import(SpringSecurityConfigs.class)
@TestPropertySource(properties = {
        "cloudinary.cloud-name=test",
        "cloudinary.api-key=test",
        "cloudinary.api-secret=test"
})
class SecurityAuthorizationMockMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JwtFilter jwtFilter;

    @BeforeEach
    void passJwtFilterThrough() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtFilter).doFilter(any(ServletRequest.class), any(ServletResponse.class), any(FilterChain.class));
    }

    @Test
    void publicCourse_isAccessibleWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/courses/probe"))
                .andExpect(status().isOk());
    }

    @Test
    void studentApi_withoutAuthentication_returns401() throws Exception {
        mockMvc.perform(get("/api/student/probe"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void studentRole_canAccessStudentApi() throws Exception {
        mockMvc.perform(get("/api/student/probe"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PARENT")
    void parentRole_cannotAccessStudentApi() throws Exception {
        mockMvc.perform(get("/api/student/probe"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "PARENT")
    void parentRole_canAccessParentApi() throws Exception {
        mockMvc.perform(get("/api/parent/probe"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void studentRole_cannotAccessParentApi() throws Exception {
        mockMvc.perform(get("/api/parent/probe"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void teacherRole_canAccessTeacherClassApi() throws Exception {
        mockMvc.perform(get("/api/teacher/classes/probe"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void managerRole_cannotAccessTeacherClassApi() throws Exception {
        mockMvc.perform(get("/api/teacher/classes/probe"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminRole_cannotAccessTeacherClassApi() throws Exception {
        mockMvc.perform(get("/api/teacher/classes/probe"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "TEACHER")
    void teacherRole_canAccessTeacherGradingApi() throws Exception {
        mockMvc.perform(get("/api/teacher/grading/probe"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void managerRole_canAccessTeacherGradingApi() throws Exception {
        mockMvc.perform(get("/api/teacher/grading/probe"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminRole_canAccessTeacherGradingApi() throws Exception {
        mockMvc.perform(get("/api/teacher/grading/probe"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PARENT")
    void parentRole_cannotAccessTeacherGradingApi() throws Exception {
        mockMvc.perform(get("/api/teacher/grading/probe"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminRole_canAccessAdminApi() throws Exception {
        mockMvc.perform(get("/api/admin/probe"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void managerRole_cannotAccessAdminApi() throws Exception {
        mockMvc.perform(get("/api/admin/probe"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void studentRole_canCreatePayment() throws Exception {
        mockMvc.perform(post("/api/payment-transactions"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "PARENT")
    void parentRole_cannotCreatePayment() throws Exception {
        mockMvc.perform(post("/api/payment-transactions"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void managerRole_canUpdatePayment() throws Exception {
        mockMvc.perform(put("/api/payment-transactions/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminRole_canUpdatePayment() throws Exception {
        mockMvc.perform(put("/api/payment-transactions/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "STUDENT")
    void studentRole_cannotUpdatePayment() throws Exception {
        mockMvc.perform(put("/api/payment-transactions/1"))
                .andExpect(status().isForbidden());
    }

    @RestController
    static class SecurityProbeController {

        @GetMapping("/api/courses/probe")
        String publicCourse() {
            return "ok";
        }

        @GetMapping("/api/student/probe")
        String student() {
            return "ok";
        }

        @GetMapping("/api/parent/probe")
        String parent() {
            return "ok";
        }

        @GetMapping("/api/teacher/classes/probe")
        String teacherClass() {
            return "ok";
        }

        @GetMapping("/api/teacher/grading/probe")
        String teacherGrading() {
            return "ok";
        }

        @GetMapping("/api/admin/probe")
        String admin() {
            return "ok";
        }

        @PostMapping("/api/payment-transactions")
        String createPayment() {
            return "ok";
        }

        @PutMapping("/api/payment-transactions/{id}")
        String updatePayment(@PathVariable Integer id) {
            return "ok";
        }
    }
}
