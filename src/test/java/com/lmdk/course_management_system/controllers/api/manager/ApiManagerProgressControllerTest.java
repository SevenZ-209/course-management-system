package com.lmdk.course_management_system.controllers.api.api.manager;

import com.lmdk.course_management_system.controllers.api.manager.ApiManagerProgressController;
import com.lmdk.course_management_system.mappers.manager.ManagerStudentProgressMapper;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.LearningPathDetailService;
import com.lmdk.course_management_system.services.StudentLearningPathService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiManagerProgressControllerTest {

    @Mock private EnrollmentService enrollmentService;
    @Mock private StudentLearningPathService studentLearningPathService;
    @Mock private LearningPathDetailService learningPathDetailService;
    @Mock private ManagerStudentProgressMapper progressMapper;

    private ApiManagerProgressController controller;

    @BeforeEach
    void setUp() {
        controller = new ApiManagerProgressController(
                enrollmentService, studentLearningPathService,
                learningPathDetailService, progressMapper
        );
        ReflectionTestUtils.setField(controller, "pageSize", 10);
    }

    @Test
    void getProgress_emptyPage_returnsSummaryCounts() {
        when(enrollmentService.countEnrollments(anyMap())).thenAnswer(invocation -> {
            Map<String, String> params = invocation.getArgument(0);
            String progressStatus = params.get("progressStatus");
            if("IN_PROGRESS".equals(progressStatus)) return 5L;
            if("PAUSED".equals(progressStatus)) return 2L;
            if("COMPLETED".equals(progressStatus)) return 3L;
            return 12L;
        });
        when(enrollmentService.getEnrollments(anyMap())).thenReturn(List.of());
        when(studentLearningPathService.getStudentLearningPathsByStudentsAndCourses(anyList(), anyList()))
                .thenReturn(List.of());
        when(learningPathDetailService.getDetailsByLearningPaths(anyList())).thenReturn(List.of());

        var response = controller.getProgress(1, null, null, null, null);

        assertEquals(12L, response.totalRecords());
        assertEquals(5L, response.inProgressCount());
        assertEquals(2L, response.pausedCount());
        assertEquals(3L, response.completedCount());
        assertEquals(2L, response.noPathCount());
        assertTrue(response.progress().isEmpty());
        verify(studentLearningPathService).getStudentLearningPathsByStudentsAndCourses(List.of(), List.of());
    }

    @Test
    void getProgress_rejectsInvalidStatus() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> controller.getProgress(1, null, null, null, "UNKNOWN")
        );

        assertEquals("Trạng thái tiến độ không hợp lệ!", ex.getMessage());
        verifyNoInteractions(enrollmentService, studentLearningPathService, learningPathDetailService, progressMapper);
    }
}
