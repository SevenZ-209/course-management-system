package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.OnlineSession;
import com.lmdk.course_management_system.repository.OnlineSessionRepository;
import com.lmdk.course_management_system.services.OnlineSessionService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OnlineSessionServiceImpl implements OnlineSessionService {

    private final OnlineSessionRepository sessionRepository;

    @Override
    public OnlineSession getSessionById(Integer id) {
        return sessionRepository.getSessionById(id);
    }

    @Override
    public OnlineSession addSession(OnlineSession onlineSession) {
        validateSession(onlineSession);
        return sessionRepository.addSession(onlineSession);
    }

    @Override
    public void updateSession(OnlineSession onlineSession) {
        validateSession(onlineSession);
        sessionRepository.updateSession(onlineSession);
    }

    @Override
    public List<OnlineSession> getSessions(Map<String, String> params) {
        return sessionRepository.getSessions(params);
    }

    @Override
    public List<OnlineSession> getSessionsByClass(Integer classId) {
        return sessionRepository.getSessionsByClass(classId);
    }

    @Override
    public List<OnlineSession> getEndedSessionsByStudent(Integer studentId) {
        return sessionRepository.getEndedSessionsByStudent(studentId);
    }

    @Override
    public long countSessions(Map<String, String> params) {
        return sessionRepository.countSessions(params);
    }

    @Override
    public List<OnlineSession> getAllSessions() {
        return sessionRepository.getAllSessions();
    }

    private void validateSession(OnlineSession session) {
        if (session.getTitle() == null || session.getTitle().trim().isBlank())
            throw new IllegalArgumentException("Tiêu đề buổi học không được để trống!");

        if (session.getCourseClass() == null)
            throw new IllegalArgumentException("Vui lòng chọn lớp học!");

        if (session.getTeacher() == null)
            throw new IllegalArgumentException("Vui lòng chọn giáo viên!");

        if (session.getStartTime() == null || session.getEndTime() == null)
            throw new IllegalArgumentException("Vui lòng nhập thời gian buổi học!");

        if (!session.getEndTime().isAfter(session.getStartTime()))
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu!");

        if (session.getStartTime().toLocalDate().isBefore(session.getCourseClass().getStartDate())
                || session.getEndTime().toLocalDate().isAfter(session.getCourseClass().getEndDate()))
            throw new IllegalArgumentException("Buổi học phải nằm trong thời gian của lớp học!");

        if (session.getMeetingUrl() == null || session.getMeetingUrl().trim().isBlank())
            throw new IllegalArgumentException("Link phòng học không được để trống!");

        session.setTitle(session.getTitle().trim());
        session.setMeetingUrl(session.getMeetingUrl().trim());
    }
}