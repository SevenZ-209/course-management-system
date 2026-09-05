package com.lmdk.course_management_system.selectionux;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SelectionUxBatch3TemplateContractTest {

    @Test
    void parentLinksTemplate_usesAsyncStudentAndParentLookup() throws Exception {
        String html = Files.readString(Path.of("src/main/resources/templates/admin/parent-links.html"));

        assertTrue(html.contains("/admin/users/student-options"));
        assertTrue(html.contains("/admin/users/parent-options"));
        assertTrue(html.contains("data-async-user-lookup"));
        assertFalse(html.contains("th:each=\"student : ${students}\""));
        assertFalse(html.contains("th:each=\"parent : ${parents}\""));
    }

    @Test
    void studentLearningPathTemplate_usesAsyncStudentAndCourseDependentPath() throws Exception {
        String html = Files.readString(Path.of("src/main/resources/templates/admin/student-learning-paths.html"));

        assertTrue(html.contains("id=\"slpFilterCourse\""));
        assertTrue(html.contains("id=\"slpFilterPath\""));
        assertTrue(html.contains("/admin/learning-paths/options?courseId="));
        assertTrue(html.contains("/admin/users/student-options"));
        assertFalse(html.contains("th:each=\"student : ${students}\""));
    }

    @Test
    void assignedAssignmentTemplate_avoidsGlobalStudentAndProgressOptions() throws Exception {
        String html = Files.readString(Path.of("src/main/resources/templates/admin/assigned-assignments.html"));

        assertTrue(html.contains("id=\"assignedFilterCourse\""));
        assertTrue(html.contains("/admin/learning-paths/options?courseId="));
        assertTrue(html.contains("id=\"manualStudentLookup\""));
        assertTrue(html.contains("id=\"releaseStudentLookup\""));
        assertTrue(html.contains("/admin/assigned-assignments/in-progress-options?studentId="));
        assertFalse(html.contains("th:each=\"student : ${students}\""));
        assertFalse(html.contains("th:each=\"progress : ${progresses}\""));
    }
}
