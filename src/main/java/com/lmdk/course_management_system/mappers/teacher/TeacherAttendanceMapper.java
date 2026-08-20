package com.lmdk.course_management_system.mappers.teacher;

import com.lmdk.course_management_system.dto.teacher.attendance.TeacherAttendanceResponse;
import com.lmdk.course_management_system.pojo.Attendance;
import com.lmdk.course_management_system.pojo.User;

import org.springframework.stereotype.Component;

@Component
public class TeacherAttendanceMapper {

    public TeacherAttendanceResponse toResponse(
            User student,
            Attendance attendance
    ) {
        return new TeacherAttendanceResponse(
                student.getId(),
                student.getFullName(),
                student.getUsername(),

                attendance == null
                        ? null
                        : attendance.getId(),

                getStatus(attendance),

                attendance == null
                        ? null
                        : attendance.getAttendedAt(),

                attendance == null
                        ? null
                        : attendance.getNote()
        );
    }

    private String getStatus(Attendance attendance) {
        if(attendance == null)
            return "NOT_MARKED";

        return Boolean.TRUE.equals(attendance.getPresent())
                ? "PRESENT"
                : "ABSENT";
    }
}