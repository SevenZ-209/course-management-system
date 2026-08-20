package com.lmdk.course_management_system.mappers.admin;

import com.lmdk.course_management_system.dto.admin.attendance.AdminAttendanceResponse;
import com.lmdk.course_management_system.pojo.Attendance;

import org.springframework.stereotype.Component;

@Component
public class AdminAttendanceMapper {

    public AdminAttendanceResponse toResponse(
            Attendance attendance
    ) {
        var session = attendance.getOnlineSession();
        var courseClass = session.getCourseClass();
        var student = attendance.getStudent();

        return new AdminAttendanceResponse(
                attendance.getId(),

                session.getId(),
                session.getTitle(),

                courseClass.getId(),
                courseClass.getName(),

                courseClass.getCourse().getId(),
                courseClass.getCourse().getName(),

                student.getId(),
                student.getFullName(),
                student.getUsername(),

                attendance.getPresent(),
                attendance.getAttendedAt(),
                attendance.getNote()
        );
    }
}