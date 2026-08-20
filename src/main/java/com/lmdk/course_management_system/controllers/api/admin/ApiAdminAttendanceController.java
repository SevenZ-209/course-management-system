package com.lmdk.course_management_system.controllers.api.admin;

import com.lmdk.course_management_system.dto.admin.attendance.*;
import com.lmdk.course_management_system.mappers.admin.AdminAttendanceMapper;
import com.lmdk.course_management_system.pojo.Attendance;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.OnlineSession;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.AttendanceService;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.OnlineSessionService;
import com.lmdk.course_management_system.services.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/attendances")
@RequiredArgsConstructor
public class ApiAdminAttendanceController {

    private final AttendanceService attendanceService;
    private final OnlineSessionService sessionService;
    private final UserService userService;
    private final EnrollmentService enrollmentService;
    private final AdminAttendanceMapper adminAttendanceMapper;

    @Value("${attendances.page-size:10}")
    private int pageSize;

    @GetMapping
    public AdminAttendancePageResponse getAttendances(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) Integer sessionId,
            @RequestParam(required = false) Integer classId,
            @RequestParam(required = false) Boolean present
    ) {
        page = Math.max(page, 1);

        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));

        if(kw != null && !kw.isBlank())
            params.put("kw", kw.trim());

        if(sessionId != null)
            params.put("sessionId", String.valueOf(sessionId));

        if(classId != null)
            params.put("classId", String.valueOf(classId));

        if(present != null)
            params.put("present", String.valueOf(present));

        long totalRecords =
                attendanceService.countAttendances(params);

        int totalPages = Math.max(
                (int) Math.ceil((double) totalRecords / pageSize),
                1
        );

        if(page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        return new AdminAttendancePageResponse(
                attendanceService.getAttendances(params)
                        .stream()
                        .map(adminAttendanceMapper::toResponse)
                        .toList(),
                page,
                totalPages,
                totalRecords
        );
    }

    @PostMapping
    public AdminAttendanceActionResponse addAttendance(
            @RequestBody CreateAttendanceRequest request
    ) {
        OnlineSession session =
                requireSession(request.sessionId());

        User student =
                requireStudent(request.studentId());

        if(request.present() == null)
            throw new IllegalArgumentException(
                    "Trạng thái điểm danh không được để trống!"
            );

        Enrollment enrollment =
                enrollmentService.getEnrollment(
                        student.getId(),
                        session.getCourseClass().getId()
                );

        if(enrollment == null
                || enrollment.getStatus()
                != Enrollment.EnrollmentStatus.ACTIVE)
            throw new IllegalArgumentException(
                    "Học viên không thuộc lớp học của buổi này!"
            );

        Attendance attendance = new Attendance();
        attendance.setOnlineSession(session);
        attendance.setStudent(student);
        attendance.setPresent(request.present());
        attendance.setNote(request.note());

        attendanceService.addAttendance(attendance);

        return new AdminAttendanceActionResponse(
                attendance.getId(),
                "Điểm danh thành công!"
        );
    }

    @PutMapping("/{attendanceId}")
    public AdminAttendanceActionResponse updateAttendance(
            @PathVariable Integer attendanceId,
            @RequestBody UpdateAttendanceRequest request
    ) {
        Attendance attendance =
                requireAttendance(attendanceId);

        if(request.present() == null)
            throw new IllegalArgumentException(
                    "Trạng thái điểm danh không được để trống!"
            );

        attendance.setPresent(request.present());
        attendance.setNote(request.note());

        attendanceService.updateAttendance(attendance);

        return new AdminAttendanceActionResponse(
                attendanceId,
                "Cập nhật điểm danh thành công!"
        );
    }

    @GetMapping("/students")
    public List<AdminAttendanceStudentOptionResponse> getStudentsBySession(
            @RequestParam Integer sessionId
    ) {
        OnlineSession session =
                requireSession(sessionId);

        return enrollmentService
                .getActiveEnrollmentsByClass(
                        session.getCourseClass().getId()
                )
                .stream()
                .map(enrollment -> {
                    User student = enrollment.getStudent();

                    return new AdminAttendanceStudentOptionResponse(
                            student.getId(),
                            student.getFullName(),
                            student.getUsername()
                    );
                })
                .toList();
    }

    private Attendance requireAttendance(
            Integer attendanceId
    ) {
        Attendance attendance =
                attendanceService
                        .getAttendanceById(attendanceId);

        if(attendance == null)
            throw new IllegalArgumentException(
                    "Không tìm thấy dữ liệu điểm danh!"
            );

        return attendance;
    }

    private OnlineSession requireSession(
            Integer sessionId
    ) {
        if(sessionId == null)
            throw new IllegalArgumentException(
                    "Vui lòng chọn buổi học!"
            );

        OnlineSession session =
                sessionService.getSessionById(sessionId);

        if(session == null)
            throw new IllegalArgumentException(
                    "Buổi học không tồn tại!"
            );

        return session;
    }

    private User requireStudent(
            Integer studentId
    ) {
        if(studentId == null)
            throw new IllegalArgumentException(
                    "Vui lòng chọn học viên!"
            );

        User student =
                userService.getUserById(studentId);

        if(student == null
                || student.getRole() != User.UserRole.STUDENT
                || student.getStatus() != User.UserStatus.ACTIVE)
            throw new IllegalArgumentException(
                    "Học viên không hợp lệ!"
            );

        return student;
    }
}