package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.pojo.Attendance;

import java.util.List;
import java.util.Map;

public interface AttendanceService {

    Attendance getAttendanceById(Integer id);

    Attendance getAttendance(Integer sessionId, Integer studentId);

    Attendance addAttendance(Attendance attendance);

    void updateAttendance(Attendance attendance);

    List<Attendance> saveAttendances(List<Attendance> attendances, Integer classId, Integer sessionId);

    List<Attendance> getAttendances(Map<String, String> params);

    List<Attendance> getAttendancesBySession(Integer sessionId);

    List<Attendance> getAttendancesByStudentAndSessionIds(Integer studentId, List<Integer> sessionIds);

    long countAttendances(Map<String, String> params);
}