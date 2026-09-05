package com.lmdk.course_management_system.controllers.api;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ThymeleafAdminTemplateContractTest {

    private String template(String name) throws IOException {
        return Files.readString(Path.of("src/main/resources/templates", name));
    }

    @Test
    void enrollmentTemplate_doesNotAllowDirectActiveBypass() throws Exception {
        String html = template("admin/enrollments.html");
        assertTrue(html.contains("/admin/enrollments/cancel"));
        assertFalse(html.contains("/admin/enrollments/update-status"));
        assertTrue(html.contains("MANUAL / PENDING"));
    }

    @Test
    void paymentTemplate_onlyProcessesPendingThroughGuardedEndpoint() throws Exception {
        String html = template("admin/payment-transactions.html");
        assertFalse(html.contains("addTransactionModal"));
        assertFalse(html.contains("/admin/payment-transactions/add"));
        assertTrue(html.contains("/admin/payment-transactions/update-status"));
        assertTrue(html.contains("status.name() == 'PENDING'"));
    }

    @Test
    void parentLinkTemplate_supportsUnlinkedStatus() throws Exception {
        String html = template("admin/parent-links.html");
        assertTrue(html.contains("value=\"UNLINKED\""));
        assertTrue(html.contains("Đã hủy liên kết"));
    }

    @Test
    void baseAndReportTemplate_exposeAdminReporting() throws Exception {
        String base = template("base.html");
        String report = template("admin/reports.html");
        assertTrue(base.contains("/admin/reports"));
        assertTrue(report.contains("Báo cáo hệ thống"));
        assertTrue(report.contains("revenueTrend"));
        assertTrue(report.contains("LAST_7_DAYS"));
        assertTrue(report.contains("PREVIOUS_YEAR"));
    }
    @Test
    void allAdminListTemplates_keepFilterParamsWhenPaging() throws Exception {
        java.util.Map<String, String[]> contracts = new java.util.LinkedHashMap<>();
        contracts.put("admin/users.html", new String[]{"kw", "role", "status"});
        contracts.put("admin/categories.html", new String[]{"kw", "status"});
        contracts.put("admin/courses.html", new String[]{"kw", "categoryId", "status"});
        contracts.put("admin/course-modules.html", new String[]{"kw", "courseId", "status"});
        contracts.put("admin/lessons.html", new String[]{"kw", "courseId", "moduleId"});
        contracts.put("admin/classes.html", new String[]{"kw", "courseId", "teacherId", "status"});
        contracts.put("admin/online-sessions.html", new String[]{"kw", "classId", "teacherId", "date"});
        contracts.put("admin/attendances.html", new String[]{"kw", "classId", "sessionId", "present"});
        contracts.put("admin/enrollments.html", new String[]{"kw", "courseId", "classId", "status"});
        contracts.put("admin/payment-transactions.html", new String[]{"kw", "courseId", "status", "date"});
        contracts.put("admin/parent-links.html", new String[]{"kw", "studentId", "parentId", "status"});
        contracts.put("admin/learning-paths.html", new String[]{"kw", "courseId", "status"});
        contracts.put("admin/learning-path-details.html", new String[]{"kw", "courseId", "learningPathId"});
        contracts.put("admin/student-learning-paths.html", new String[]{"kw", "courseId", "learningPathId", "status"});
        contracts.put("admin/assignments.html", new String[]{"kw", "courseId", "type", "status"});
        contracts.put("admin/questions.html", new String[]{"kw", "courseId", "assignmentId", "type"});
        contracts.put("admin/answers.html", new String[]{"kw", "courseId", "assignmentId", "type"});
        contracts.put("admin/assigned-assignments.html", new String[]{"kw", "courseId", "learningPathId", "status", "date"});

        for(var entry : contracts.entrySet()) {
            String html = template(entry.getKey());
            assertTrue(html.contains("page=${pageNumber}"), entry.getKey() + " thiếu page number trong pagination");
            for(String param : entry.getValue()) {
                assertTrue(html.contains("name=\"" + param + "\""), entry.getKey() + " thiếu filter " + param);
                assertTrue(html.contains(param + "=${" + param + "}"), entry.getKey() + " không giữ filter " + param + " khi phân trang");
            }
        }
    }

}
