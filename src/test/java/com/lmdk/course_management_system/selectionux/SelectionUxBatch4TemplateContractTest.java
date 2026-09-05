package com.lmdk.course_management_system.selectionux;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class SelectionUxBatch4TemplateContractTest {

    @Test
    void teacherSelectors_useAsyncLookupInsteadOfRenderingEveryTeacher() throws Exception {
        String classes = read("templates/admin/classes.html");
        String sessions = read("templates/admin/online-sessions.html");

        assertTrue(classes.contains("data-async-user-lookup"));
        assertTrue(sessions.contains("data-async-user-lookup"));
        assertFalse(classes.contains("th:each=\"teacher : ${teachers}\""));
        assertFalse(sessions.contains("th:each=\"teacher : ${teachers}\""));
    }

    @Test
    void dependentSelectors_areSharedAcrossLessonAssignmentQuestionAnswerAndLearningPathScreens() throws Exception {
        String js = read("static/js/admin-selection.js");
        String combined = read("templates/admin/lessons.html")
                + read("templates/admin/assignments.html")
                + read("templates/admin/questions.html")
                + read("templates/admin/answers.html")
                + read("templates/admin/learning-path-details.html");

        assertTrue(js.contains("data-dependent-select"));
        assertTrue(js.contains("initDependentSelect"));
        assertTrue(combined.contains("/admin/lessons/modules"));
        assertTrue(combined.contains("/admin/assignments/lessons"));
        assertTrue(combined.contains("/admin/questions/assignments"));
        assertTrue(combined.contains("/admin/answers/assignments"));
        assertTrue(combined.contains("/admin/learning-path-details/paths"));
    }

    private String read(String relativePath) throws Exception {
        return Files.readString(Path.of("src/main/resources").resolve(relativePath));
    }
}
