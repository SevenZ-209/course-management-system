package com.lmdk.course_management_system.repository;

import com.lmdk.course_management_system.pojo.OnlineSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface OnlineSessionRepository {

    OnlineSession getSessionById(Integer id);

    OnlineSession addSession(OnlineSession onlineSession);

    void updateSession(OnlineSession onlineSession);

    void lockScheduleResources(Integer classId, Integer teacherId);

    boolean existsClassScheduleConflict(Integer classId, LocalDateTime startTime, LocalDateTime endTime, Integer excludeSessionId);

    boolean existsTeacherScheduleConflict(Integer teacherId, LocalDateTime startTime, LocalDateTime endTime, Integer excludeSessionId);

    List<OnlineSession> getSessions(Map<String, String> params);

    List<OnlineSession> getSessionsByClass(Integer classId);

    List<OnlineSession> getEndedSessionsByStudent(Integer studentId);

    long countSessions(Map<String, String> params);

    List<OnlineSession> getAllSessions();
}