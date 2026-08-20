package com.lmdk.course_management_system.repository;

import com.lmdk.course_management_system.pojo.Attendance;

import java.util.List;
import java.util.Map;

public interface AttendanceRepository {

    Attendance getAttendanceById(Integer id);

    Attendance getAttendance(Integer sessionId, Integer studentId);

    Attendance addAttendance(Attendance attendance);

    void updateAttendance(Attendance attendance);

    List<Attendance> getAttendances(Map<String, String> params);

    List<Attendance> getAttendancesBySession(Integer sessionId);

    long countAttendances(Map<String, String> params);

    boolean existsAttendance(Integer sessionId, Integer studentId);
}