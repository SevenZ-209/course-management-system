package com.lmdk.course_management_system.mappers.manager;

import com.lmdk.course_management_system.dto.manager.progress.ManagerStudentProgressResponse;
import com.lmdk.course_management_system.dto.teacher.classinfo.TeacherStudentProgressResponse;
import com.lmdk.course_management_system.mappers.teacher.TeacherStudentProgressMapper;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.LearningPathDetail;
import com.lmdk.course_management_system.pojo.StudentLearningPath;
import com.lmdk.course_management_system.pojo.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ManagerStudentProgressMapper {

    private final TeacherStudentProgressMapper progressMapper;

    public ManagerStudentProgressResponse toResponse(
            Enrollment enrollment,
            StudentLearningPath progress,
            List<LearningPathDetail> details
    ) {
        User student = enrollment.getStudent();
        CourseClass courseClass = enrollment.getCourseClass();
        User teacher = courseClass.getTeacher();
        TeacherStudentProgressResponse base = progressMapper.toResponse(student, progress, details);

        return new ManagerStudentProgressResponse(
                enrollment.getId(),
                base.studentId(), base.studentName(), base.username(),
                courseClass.getId(), courseClass.getName(),
                courseClass.getCourse().getId(), courseClass.getCourse().getName(),
                teacher == null ? null : teacher.getId(),
                teacher == null ? null : teacher.getFullName(),
                base.studentLearningPathId(), base.learningPathId(), base.learningPathName(),
                base.learningPathStatus(), base.currentDetailId(), base.currentAssignmentId(),
                base.currentAssignmentName(), base.completedDetails(), base.totalDetails()
        );
    }
}
