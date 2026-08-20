package com.lmdk.course_management_system.services;

import com.lmdk.course_management_system.pojo.OnlineSession;

import java.util.List;
import java.util.Map;

public interface OnlineSessionService {

    OnlineSession getSessionById(Integer id);

    OnlineSession addSession(OnlineSession onlineSession);

    void updateSession(OnlineSession onlineSession);

    List<OnlineSession> getSessions(Map<String, String> params);

    List<OnlineSession> getSessionsByClass(Integer classId);

    long countSessions(Map<String, String> params);

    List<OnlineSession> getAllSessions();
}