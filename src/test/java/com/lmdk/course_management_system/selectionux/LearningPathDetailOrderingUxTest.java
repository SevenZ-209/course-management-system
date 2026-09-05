package com.lmdk.course_management_system.selectionux;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LearningPathDetailOrderingUxTest {

    @Test
    void repositoryOrdersByCourseThenPathThenOrder() throws Exception {
        String source = Files.readString(Path.of("src/main/java/com/lmdk/course_management_system/repository/impl/LearningPathDetailRepositoryImpl.java"))
                .replaceAll("\\s+", "");

        String expected = ".orderBy(cb.asc(root.get(\"learningPath\").get(\"course\").get(\"name\")),"
                + "cb.asc(root.get(\"learningPath\").get(\"name\")),"
                + "cb.asc(root.get(\"orderNumber\")),cb.asc(root.get(\"id\")))";

        assertTrue(source.contains(expected), "Learning Path Detail phải sort Course -> Path -> Order -> ID");
    }

    @Test
    void thymeleafHidesTechnicalIdAndShowsCourseBeforePath() throws Exception {
        String html = Files.readString(Path.of("src/main/resources/templates/admin/learning-path-details.html"));

        assertFalse(html.contains("<th>ID</th>"), "Không nên hiển thị technical ID trong bảng");
        assertTrue(html.indexOf("<th>Khóa học</th>") < html.indexOf("<th>Lộ trình</th>"),
                "Khóa học phải hiển thị trước Lộ trình");
        assertTrue(html.contains("colspan=\"7\""));
    }
}
