package com.lmdk.course_management_system.selectionux;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AnswerOrderingUxTest {

    @Test
    void repositoryOrdersByCourseAssignmentQuestionAndAnswerOrder() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/lmdk/course_management_system/repository/impl/AnswerRepositoryImpl.java"))
                .replaceAll("\\s+", "");

        String expected = ".orderBy(cb.asc(root.get(\"question\").get(\"assignment\").get(\"course\").get(\"name\")),"
                + "cb.asc(root.get(\"question\").get(\"assignment\").get(\"name\")),"
                + "cb.asc(root.get(\"question\").get(\"orderNumber\")),"
                + "cb.asc(root.get(\"orderNumber\")),cb.asc(root.get(\"id\")))";

        assertTrue(source.contains(expected),
                "Answer phải sort Course -> Assignment -> Question order -> Answer order -> ID");
    }

    @Test
    void thymeleafHidesTechnicalAnswerId() throws Exception {
        String html = Files.readString(Path.of("src/main/resources/templates/admin/answers.html"));

        assertFalse(html.contains("<th>ID</th>"), "Không nên hiển thị technical ID trong bảng Answer");
        assertFalse(html.contains("<td th:text=\"${answer.id}\"></td>"), "Không nên render answer ID trong mỗi dòng");
        assertTrue(html.contains("colspan=\"7\""));
    }
}
