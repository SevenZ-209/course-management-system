package com.lmdk.course_management_system.controllers.api.manager;

import com.lmdk.course_management_system.dto.admin.onlinesession.AdminOnlineSessionActionResponse;
import com.lmdk.course_management_system.dto.admin.onlinesession.AdminOnlineSessionPageResponse;
import com.lmdk.course_management_system.dto.admin.onlinesession.AdminOnlineSessionResponse;
import com.lmdk.course_management_system.dto.admin.onlinesession.OnlineSessionRequest;
import com.lmdk.course_management_system.mappers.admin.AdminOnlineSessionMapper;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.OnlineSession;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.CourseClassService;
import com.lmdk.course_management_system.services.OnlineSessionService;
import com.lmdk.course_management_system.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manager/online-sessions")
@RequiredArgsConstructor
public class ApiManagerOnlineSessionController {

    private final OnlineSessionService sessionService;
    private final CourseClassService classService;
    private final UserService userService;
    private final AdminOnlineSessionMapper sessionMapper;

    @Value("${online-sessions.page-size:10}")
    private int pageSize;

    @GetMapping
    public AdminOnlineSessionPageResponse getSessions(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(required = false) String kw,
            @RequestParam(required = false) Integer courseId,
            @RequestParam(required = false) Integer classId,
            @RequestParam(required = false) Integer teacherId,
            @RequestParam(required = false) String date
    ) {
        page = Math.max(page, 1);
        Map<String, String> params = new HashMap<>();
        params.put("page", String.valueOf(page));
        if(kw != null && !kw.isBlank()) params.put("kw", kw.trim());
        if(courseId != null) params.put("courseId", String.valueOf(courseId));
        if(classId != null) params.put("classId", String.valueOf(classId));
        if(teacherId != null) params.put("teacherId", String.valueOf(teacherId));
        if(date != null && !date.isBlank()) params.put("date", date);

        long totalRecords = sessionService.countSessions(params);
        int totalPages = Math.max((int) Math.ceil((double) totalRecords / pageSize), 1);
        if(page > totalPages) {
            page = totalPages;
            params.put("page", String.valueOf(page));
        }

        return new AdminOnlineSessionPageResponse(
                sessionService.getSessions(params).stream().map(sessionMapper::toResponse).toList(),
                page, totalPages, totalRecords
        );
    }

    @PostMapping
    public AdminOnlineSessionActionResponse addSession(@RequestBody OnlineSessionRequest request) {
        CourseClass courseClass = requireClass(request.classId());
        User teacher = requireTeacher(request.teacherId());

        OnlineSession session = new OnlineSession();
        apply(session, request, courseClass, teacher);
        sessionService.addSession(session);

        return new AdminOnlineSessionActionResponse(session.getId(), "Thêm buổi học thành công!");
    }

    @PutMapping("/{sessionId}")
    public AdminOnlineSessionActionResponse updateSession(
            @PathVariable Integer sessionId,
            @RequestBody OnlineSessionRequest request
    ) {
        requireSession(sessionId);
        CourseClass courseClass = requireClass(request.classId());
        User teacher = requireTeacher(request.teacherId());

        OnlineSession candidate = new OnlineSession();
        candidate.setId(sessionId);
        apply(candidate, request, courseClass, teacher);
        sessionService.updateSession(candidate);

        return new AdminOnlineSessionActionResponse(sessionId, "Cập nhật buổi học thành công!");
    }

    @GetMapping("/options")
    public List<AdminOnlineSessionResponse> getSessionOptions(
            @RequestParam(required = false) Integer classId
    ) {
        var sessions = classId == null
                ? sessionService.getAllSessions()
                : sessionService.getSessionsByClass(classId);
        return sessions.stream().map(sessionMapper::toResponse).toList();
    }

    private void apply(
            OnlineSession session,
            OnlineSessionRequest request,
            CourseClass courseClass,
            User teacher
    ) {
        if(request.title() == null || request.title().isBlank())
            throw new IllegalArgumentException("Tiêu đề buổi học không được để trống!");
        if(request.startTime() == null || request.endTime() == null)
            throw new IllegalArgumentException("Vui lòng nhập thời gian bắt đầu và kết thúc!");
        if(!request.endTime().isAfter(request.startTime()))
            throw new IllegalArgumentException("Thời gian kết thúc phải sau thời gian bắt đầu!");
        if(request.meetingUrl() == null || request.meetingUrl().isBlank())
            throw new IllegalArgumentException("Vui lòng nhập link phòng học!");

        session.setTitle(request.title().trim());
        session.setCourseClass(courseClass);
        session.setTeacher(teacher);
        session.setStartTime(request.startTime());
        session.setEndTime(request.endTime());
        session.setMeetingUrl(request.meetingUrl().trim());
    }

    private OnlineSession requireSession(Integer sessionId) {
        OnlineSession session = sessionService.getSessionById(sessionId);
        if(session == null) throw new IllegalArgumentException("Không tìm thấy buổi học!");
        return session;
    }

    private CourseClass requireClass(Integer classId) {
        if(classId == null) throw new IllegalArgumentException("Vui lòng chọn lớp học!");
        CourseClass courseClass = classService.getClassById(classId);
        if(courseClass == null) throw new IllegalArgumentException("Lớp học không tồn tại!");
        if(courseClass.getStatus() == CourseClass.ClassStatus.CANCELED)
            throw new IllegalArgumentException("Không thể tạo buổi học cho lớp đã hủy!");
        return courseClass;
    }

    private User requireTeacher(Integer teacherId) {
        if(teacherId == null) throw new IllegalArgumentException("Vui lòng chọn giáo viên!");
        User teacher = userService.getUserById(teacherId);
        if(teacher == null || teacher.getRole() != User.UserRole.TEACHER
                || teacher.getStatus() != User.UserStatus.ACTIVE)
            throw new IllegalArgumentException("Giáo viên không hợp lệ!");
        return teacher;
    }
}
