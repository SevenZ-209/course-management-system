package com.lmdk.course_management_system.classes;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CourseClassTemplateContractTest {

    @Test
    void thymeleafClassTemplate_onlyAllowsManualCancel() throws Exception {
        String html = Files.readString(Path.of("src/main/resources/templates/admin/classes.html"));

        assertTrue(html.contains("name=\"status\" value=\"CANCELED\""));
        assertTrue(html.contains("Hủy lớp"));
        assertFalse(html.contains("th:selected=\"${courseClass.status.name() == 'UPCOMING'}\""));
        assertFalse(html.contains("th:selected=\"${courseClass.status.name() == 'ACTIVE'}\""));
        assertFalse(html.contains("th:selected=\"${courseClass.status.name() == 'COMPLETED'}\""));
    }
}
