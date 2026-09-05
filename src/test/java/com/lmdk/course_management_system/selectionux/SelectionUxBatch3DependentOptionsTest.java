package com.lmdk.course_management_system.selectionux;

import com.lmdk.course_management_system.controllers.AssignedAssignmentController;
import com.lmdk.course_management_system.controllers.LearningPathController;
import com.lmdk.course_management_system.controllers.api.admin.ApiAdminLearningPathController;
import com.lmdk.course_management_system.controllers.api.admin.ApiAdminStudentLearningPathController;
import com.lmdk.course_management_system.mappers.admin.AdminLearningPathMapper;
import com.lmdk.course_management_system.mappers.admin.AdminStudentLearningPathMapper;
import com.lmdk.course_management_system.pojo.*;
import com.lmdk.course_management_system.services.*;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class SelectionUxBatch3DependentOptionsTest {

    @Test
    void thymeleafLearningPathOptions_loadOnlySelectedCourse() {
        LearningPathService service = mock(LearningPathService.class);
        when(service.getLearningPathsByCourse(4)).thenReturn(List.of(path(8, 4)));

        LearningPathController controller = new LearningPathController(service, mock(CourseService.class));
        var result = controller.getOptions(4);

        assertEquals(1, result.size());
        assertEquals(8, result.get(0).get("id"));
        verify(service).getLearningPathsByCourse(4);
        verify(service, never()).getAllLearningPaths();
    }

    @Test
    void reactLearningPathOptions_withCourseLoadsOnlySelectedCourse() {
        LearningPathService service = mock(LearningPathService.class);
        when(service.getLearningPathsByCourse(5)).thenReturn(List.of(path(9, 5)));

        ApiAdminLearningPathController controller = new ApiAdminLearningPathController(
                service, mock(CourseService.class), mock(AdminLearningPathMapper.class));
        var result = controller.getOptions(5);

        assertEquals(1, result.size());
        verify(service).getLearningPathsByCourse(5);
        verify(service, never()).getAllLearningPaths();
    }

    @Test
    void reactInProgressOptions_areLoadedForSelectedStudentOnly() {
        StudentLearningPathService service = mock(StudentLearningPathService.class);
        StudentLearningPath active = progress(50, 100, StudentLearningPath.ProgressStatus.IN_PROGRESS, true);
        StudentLearningPath paused = progress(51, 100, StudentLearningPath.ProgressStatus.PAUSED, true);
        when(service.getStudentLearningPathsByStudent(100)).thenReturn(List.of(active, paused));

        ApiAdminStudentLearningPathController controller = new ApiAdminStudentLearningPathController(
                service, mock(LearningPathService.class), mock(EnrollmentService.class),
                mock(AdminStudentLearningPathMapper.class));
        var result = controller.getInProgressOptions(100);

        assertEquals(1, result.size());
        verify(service).getStudentLearningPathsByStudent(100);
        verify(service, never()).getInProgressStudentLearningPaths();
    }

    @Test
    void thymeleafReleaseOptions_areLoadedForSelectedStudentOnly() {
        StudentLearningPathService progressService = mock(StudentLearningPathService.class);
        StudentLearningPath active = progress(60, 101, StudentLearningPath.ProgressStatus.IN_PROGRESS, true);
        StudentLearningPath completed = progress(61, 101, StudentLearningPath.ProgressStatus.COMPLETED, true);
        when(progressService.getStudentLearningPathsByStudent(101)).thenReturn(List.of(active, completed));

        AssignedAssignmentController controller = new AssignedAssignmentController(
                mock(AssignedAssignmentService.class), progressService, mock(LearningPathService.class),
                mock(EnrollmentService.class), mock(AssignmentService.class), mock(CourseService.class),
                mock(UserService.class));
        var result = controller.getInProgressOptions(101);

        assertEquals(1, result.size());
        assertEquals(60, result.get(0).get("id"));
        verify(progressService).getStudentLearningPathsByStudent(101);
        verify(progressService, never()).getInProgressStudentLearningPaths();
    }

    private LearningPath path(Integer id, Integer courseId) {
        Course course = new Course();
        course.setId(courseId);
        course.setName("Course " + courseId);
        LearningPath path = new LearningPath();
        path.setId(id);
        path.setName("Path " + id);
        path.setCourse(course);
        return path;
    }

    private StudentLearningPath progress(Integer id, Integer studentId,
                                         StudentLearningPath.ProgressStatus status, boolean withCurrentDetail) {
        User student = new User();
        student.setId(studentId);
        student.setFullName("Student " + studentId);
        student.setUsername("student" + studentId);

        Course course = new Course();
        course.setId(4);
        course.setName("Spring");

        LearningPath path = new LearningPath();
        path.setId(7);
        path.setName("Path 7");
        path.setCourse(course);

        StudentLearningPath progress = new StudentLearningPath();
        progress.setId(id);
        progress.setStudent(student);
        progress.setLearningPath(path);
        progress.setStatus(status);

        if (withCurrentDetail) {
            Assignment assignment = new Assignment();
            assignment.setId(9);
            assignment.setName("Assignment 9");
            LearningPathDetail detail = new LearningPathDetail();
            detail.setId(12);
            detail.setOrderNumber(2);
            detail.setAssignment(assignment);
            progress.setCurrentDetail(detail);
        }
        return progress;
    }
}
