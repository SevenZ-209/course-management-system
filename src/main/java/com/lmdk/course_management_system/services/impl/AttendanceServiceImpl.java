package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.Attendance;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.repository.AttendanceRepository;
import com.lmdk.course_management_system.services.AttendanceService;

import com.lmdk.course_management_system.services.EnrollmentService;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AttendanceServiceImpl implements AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EnrollmentService enrollmentService;

    @Override
    public Attendance getAttendanceById(Integer id) {
        return attendanceRepository.getAttendanceById(id);
    }

    @Override
    public Attendance getAttendance(Integer sessionId, Integer studentId) {
        return attendanceRepository.getAttendance(sessionId, studentId);
    }

    @Override
    public Attendance addAttendance(Attendance attendance) {
        validateAttendance(attendance);

        if (attendanceRepository.existsAttendance(
                attendance.getOnlineSession().getId(),
                attendance.getStudent().getId()
        ))
            throw new IllegalArgumentException("Học viên đã có dữ liệu điểm danh trong buổi học này!");

        updateAttendedTime(attendance);

        return attendanceRepository.addAttendance(attendance);
    }

    @Override
    public void updateAttendance(Attendance attendance) {
        validateAttendance(attendance);
        updateAttendedTime(attendance);
        attendanceRepository.updateAttendance(attendance);
    }

    @Override
    public List<Attendance> getAttendances(Map<String, String> params) {
        return attendanceRepository.getAttendances(params);
    }

    @Override
    public List<Attendance> getAttendancesBySession(Integer sessionId) {
        return attendanceRepository.getAttendancesBySession(sessionId);
    }

    @Override
    public long countAttendances(Map<String, String> params) {
        return attendanceRepository.countAttendances(params);
    }

    private void validateAttendance(Attendance attendance) {

        if(attendance.getOnlineSession() == null)
            throw new IllegalArgumentException(
                    "Vui lòng chọn buổi học!"
            );

        if(attendance.getStudent() == null)
            throw new IllegalArgumentException(
                    "Vui lòng chọn học viên!"
            );

        if(attendance.getStudent().getRole()
                != User.UserRole.STUDENT)
            throw new IllegalArgumentException(
                    "Người dùng được chọn không phải học viên!"
            );

        if(attendance.getStudent().getStatus()
                != User.UserStatus.ACTIVE)
            throw new IllegalArgumentException(
                    "Tài khoản học viên không hoạt động!"
            );

        Integer studentId =
                attendance.getStudent().getId();

        Integer classId =
                attendance.getOnlineSession()
                        .getCourseClass()
                        .getId();

        Enrollment enrollment =
                enrollmentService.getEnrollment(
                        studentId,
                        classId
                );

        if(enrollment == null
                || enrollment.getStatus()
                != Enrollment.EnrollmentStatus.ACTIVE)
            throw new IllegalArgumentException(
                    "Học viên không thuộc lớp học này hoặc chưa được kích hoạt!"
            );
    }

    private void updateAttendedTime(Attendance attendance) {
        if (attendance.getPresent()) {
            if (attendance.getAttendedAt() == null)
                attendance.setAttendedAt(LocalDateTime.now());
        } else {
            attendance.setAttendedAt(null);
        }
    }
}