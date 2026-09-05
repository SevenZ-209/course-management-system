package com.lmdk.course_management_system.helpers;

import com.lmdk.course_management_system.dto.admin.attendance.AdminAttendanceRosterResponse;
import com.lmdk.course_management_system.dto.admin.attendance.BulkAttendanceItemRequest;
import com.lmdk.course_management_system.dto.admin.attendance.BulkAttendanceSaveRequest;
import com.lmdk.course_management_system.pojo.Attendance;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.OnlineSession;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.AttendanceService;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.OnlineSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class AttendanceRosterHelper {

    private final AttendanceService attendanceService;
    private final EnrollmentService enrollmentService;
    private final OnlineSessionService sessionService;

    public List<AdminAttendanceRosterResponse> getRoster(Integer sessionId) {
        OnlineSession session = requireSession(sessionId);
        Integer classId = session.getCourseClass().getId();

        Map<Integer, Attendance> attendanceByStudentId = new HashMap<>();
        for (Attendance attendance : attendanceService.getAttendancesBySession(sessionId))
            attendanceByStudentId.put(attendance.getStudent().getId(), attendance);

        return enrollmentService.getActiveEnrollmentsByClass(classId).stream()
                .map(Enrollment::getStudent)
                .map(student -> toResponse(student, attendanceByStudentId.get(student.getId())))
                .toList();
    }

    public List<AdminAttendanceRosterResponse> saveBulk(BulkAttendanceSaveRequest request) {
        if (request == null || request.sessionId() == null)
            throw new IllegalArgumentException("Vui lòng chọn buổi học!");
        if (request.attendances() == null || request.attendances().isEmpty())
            throw new IllegalArgumentException("Không có thay đổi điểm danh cần lưu!");

        OnlineSession session = requireSession(request.sessionId());
        Integer classId = session.getCourseClass().getId();

        Map<Integer, User> studentsById = new HashMap<>();
        for (Enrollment enrollment : enrollmentService.getActiveEnrollmentsByClass(classId))
            studentsById.put(enrollment.getStudent().getId(), enrollment.getStudent());

        Map<Integer, Attendance> existingByStudentId = new HashMap<>();
        for (Attendance attendance : attendanceService.getAttendancesBySession(session.getId()))
            existingByStudentId.put(attendance.getStudent().getId(), attendance);

        Set<Integer> requestedStudentIds = new HashSet<>();
        List<Attendance> changes = new ArrayList<>();

        for (BulkAttendanceItemRequest item : request.attendances()) {
            if (item == null || item.studentId() == null || item.present() == null)
                throw new IllegalArgumentException("Dữ liệu điểm danh không hợp lệ!");
            if (!requestedStudentIds.add(item.studentId()))
                throw new IllegalArgumentException("Danh sách điểm danh có học viên bị trùng!");

            User student = studentsById.get(item.studentId());
            if (student == null)
                throw new IllegalArgumentException("Học viên không thuộc lớp học này hoặc chưa được kích hoạt!");

            Attendance attendance = existingByStudentId.get(item.studentId());
            if (attendance == null) {
                attendance = new Attendance();
                attendance.setOnlineSession(session);
                attendance.setStudent(student);
            }

            attendance.setPresent(item.present());
            attendance.setNote(normalizeNote(item.note()));
            changes.add(attendance);
        }

        Map<Integer, Attendance> savedByStudentId = new HashMap<>();
        for (Attendance attendance : attendanceService.saveAttendances(changes, classId, session.getId()))
            savedByStudentId.put(attendance.getStudent().getId(), attendance);

        return request.attendances().stream()
                .map(item -> toResponse(studentsById.get(item.studentId()), savedByStudentId.get(item.studentId())))
                .toList();
    }

    private AdminAttendanceRosterResponse toResponse(User student, Attendance attendance) {
        return new AdminAttendanceRosterResponse(
                student.getId(),
                student.getFullName(),
                student.getUsername(),
                attendance == null ? null : attendance.getId(),
                attendance == null ? "NOT_MARKED" : Boolean.TRUE.equals(attendance.getPresent()) ? "PRESENT" : "ABSENT",
                attendance == null ? null : attendance.getAttendedAt(),
                attendance == null ? null : attendance.getNote()
        );
    }

    private OnlineSession requireSession(Integer sessionId) {
        OnlineSession session = sessionId == null ? null : sessionService.getSessionById(sessionId);
        if (session == null)
            throw new IllegalArgumentException("Buổi học không tồn tại!");
        return session;
    }

    private String normalizeNote(String note) {
        return note == null || note.isBlank() ? null : note.trim();
    }
}
