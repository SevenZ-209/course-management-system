package com.lmdk.course_management_system.selectionux;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SelectionUxBatch2TemplateContractTest {

    @Test
    void attendanceTemplate_usesClassSessionRosterBulkFlow() throws Exception {
        String html = Files.readString(Path.of("src/main/resources/templates/admin/attendances.html"));

        assertTrue(html.contains("id=\"bulkAttendanceClass\""));
        assertTrue(html.contains("id=\"bulkAttendanceSession\""));
        assertTrue(html.contains("/api/admin/attendances/roster?sessionId="));
        assertTrue(html.contains("/api/admin/attendances/bulk"));
        assertTrue(html.contains("Tất cả có mặt"));
        assertFalse(html.contains("id=\"attendanceStudent\""));
    }
}
