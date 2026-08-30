package com.lmdk.course_management_system.repository;

import com.lmdk.course_management_system.pojo.OnlineSession;

import java.util.List;
import java.util.Map;

public interface OnlineSessionRepository {

    OnlineSession getSessionById(Integer id);

    OnlineSession addSession(OnlineSession onlineSession);

    void updateSession(OnlineSession onlineSession);

    List<OnlineSession> getSessions(Map<String, String> params);

    List<OnlineSession> getSessionsByClass(Integer classId);

    List<OnlineSession> getEndedSessionsByStudent(Integer studentId);

    long countSessions(Map<String, String> params);

    List<OnlineSession> getAllSessions();
}