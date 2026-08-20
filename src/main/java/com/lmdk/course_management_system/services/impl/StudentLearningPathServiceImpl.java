package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.pojo.LearningPath;
import com.lmdk.course_management_system.pojo.LearningPathDetail;
import com.lmdk.course_management_system.pojo.StudentLearningPath;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.repository.StudentLearningPathRepository;
import com.lmdk.course_management_system.services.EnrollmentService;
import com.lmdk.course_management_system.services.LearningPathDetailService;
import com.lmdk.course_management_system.services.LearningPathService;
import com.lmdk.course_management_system.services.StudentLearningPathService;
import com.lmdk.course_management_system.services.UserService;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentLearningPathServiceImpl implements StudentLearningPathService {

    private final StudentLearningPathRepository studentLearningPathRepository;
    private final LearningPathService learningPathService;
    private final LearningPathDetailService detailService;
    private final EnrollmentService enrollmentService;
    private final UserService userService;

    @Override
    public StudentLearningPath getStudentLearningPathById(Integer id) {
        return studentLearningPathRepository.getStudentLearningPathById(id);
    }

    @Override
    public StudentLearningPath getStudentLearningPath(Integer studentId, Integer learningPathId) {
        return studentLearningPathRepository.getStudentLearningPath(studentId, learningPathId);
    }

    @Override
    public StudentLearningPath assignLearningPath(Integer studentId, Integer learningPathId) {
        User student = userService.getUserById(studentId);
        LearningPath learningPath = learningPathService.getLearningPathById(learningPathId);

        if (student == null)
            throw new IllegalArgumentException("Học viên không tồn tại!");

        if (student.getRole() != User.UserRole.STUDENT)
            throw new IllegalArgumentException("Người dùng được chọn không phải học viên!");

        if (student.getStatus() != User.UserStatus.ACTIVE)
            throw new IllegalArgumentException("Tài khoản học viên không hoạt động!");

        if (learningPath == null)
            throw new IllegalArgumentException("Lộ trình không tồn tại!");

        if (learningPath.getStatus() != LearningPath.LearningPathStatus.ACTIVE)
            throw new IllegalArgumentException("Lộ trình đang ngừng hoạt động!");

        if (!enrollmentService.existsActiveEnrollmentByStudentAndCourse(
                studentId,
                learningPath.getCourse().getId()
        ))
            throw new IllegalArgumentException(
                    "Học viên chưa đăng ký hoặc chưa được kích hoạt trong khóa học này!"
            );

        if (studentLearningPathRepository.existsStudentLearningPath(studentId, learningPathId))
            throw new IllegalArgumentException("Học viên đã được gán lộ trình này!");

        List<LearningPathDetail> details = detailService.getDetailsByLearningPath(learningPathId);

        if (details.isEmpty())
            throw new IllegalArgumentException("Lộ trình chưa có bài tập!");

        LearningPathDetail firstDetail = details.get(0);

        StudentLearningPath studentLearningPath = new StudentLearningPath();
        studentLearningPath.setStudent(student);
        studentLearningPath.setLearningPath(learningPath);
        studentLearningPath.setCurrentDetail(firstDetail);
        studentLearningPath.setStatus(StudentLearningPath.ProgressStatus.IN_PROGRESS);
        studentLearningPath.setStartedAt(LocalDateTime.now());
        studentLearningPath.setCompletedAt(null);

        return studentLearningPathRepository.addStudentLearningPath(studentLearningPath);
    }

    @Override
    public void pauseLearningPath(Integer id) {
        StudentLearningPath studentLearningPath =
                studentLearningPathRepository.getStudentLearningPathById(id);

        if (studentLearningPath == null)
            throw new IllegalArgumentException("Không tìm thấy tiến độ học!");

        if (studentLearningPath.getStatus() == StudentLearningPath.ProgressStatus.COMPLETED)
            throw new IllegalArgumentException("Lộ trình đã hoàn thành, không thể tạm dừng!");

        if (studentLearningPath.getStatus() == StudentLearningPath.ProgressStatus.PAUSED)
            throw new IllegalArgumentException("Lộ trình đang ở trạng thái tạm dừng!");

        studentLearningPath.setStatus(StudentLearningPath.ProgressStatus.PAUSED);
        studentLearningPathRepository.updateStudentLearningPath(studentLearningPath);
    }

    @Override
    public void resumeLearningPath(Integer id) {
        StudentLearningPath studentLearningPath =
                studentLearningPathRepository.getStudentLearningPathById(id);

        if (studentLearningPath == null)
            throw new IllegalArgumentException("Không tìm thấy tiến độ học!");

        if (studentLearningPath.getStatus() == StudentLearningPath.ProgressStatus.COMPLETED)
            throw new IllegalArgumentException("Lộ trình đã hoàn thành!");

        if (studentLearningPath.getStatus() != StudentLearningPath.ProgressStatus.PAUSED)
            throw new IllegalArgumentException("Lộ trình không ở trạng thái tạm dừng!");

        studentLearningPath.setStatus(StudentLearningPath.ProgressStatus.IN_PROGRESS);
        studentLearningPathRepository.updateStudentLearningPath(studentLearningPath);
    }

    @Override
    public List<StudentLearningPath> getStudentLearningPaths(Map<String, String> params) {
        return studentLearningPathRepository.getStudentLearningPaths(params);
    }

    @Override
    public List<StudentLearningPath> getStudentLearningPathsByStudent(Integer studentId) {
        return studentLearningPathRepository.getStudentLearningPathsByStudent(studentId);
    }

    @Override
    public List<StudentLearningPath> getStudentLearningPathsByLearningPath(Integer learningPathId) {
        return studentLearningPathRepository.getStudentLearningPathsByLearningPath(learningPathId);
    }

    @Override
    public long countStudentLearningPaths(Map<String, String> params) {
        return studentLearningPathRepository.countStudentLearningPaths(params);
    }

    @Override
    public List<StudentLearningPath> getInProgressStudentLearningPaths() {
        return studentLearningPathRepository.getInProgressStudentLearningPaths();
    }

    @Override
    public StudentLearningPath advanceAfterPassedDetail(
            Integer studentId,
            Integer learningPathDetailId) {

        LearningPathDetail completedDetail =
                detailService.getDetailById(learningPathDetailId);

        if (completedDetail == null)
            throw new IllegalArgumentException(
                    "Chi tiết lộ trình không tồn tại!"
            );

        Integer learningPathId =
                completedDetail.getLearningPath().getId();

        StudentLearningPath progress =
                studentLearningPathRepository.getStudentLearningPath(
                        studentId,
                        learningPathId
                );

        if (progress == null)
            throw new IllegalArgumentException(
                    "Không tìm thấy tiến độ lộ trình của học viên!"
            );

        if (progress.getStatus() == StudentLearningPath.ProgressStatus.COMPLETED)
            return progress;

        if (progress.getStatus() != StudentLearningPath.ProgressStatus.IN_PROGRESS)
            throw new IllegalArgumentException(
                    "Lộ trình của học viên không ở trạng thái đang học!"
            );

        LearningPathDetail currentDetail = progress.getCurrentDetail();

        if (currentDetail == null)
            throw new IllegalStateException(
                    "Lộ trình không có bài hiện tại!"
            );

        if (!currentDetail.getId().equals(completedDetail.getId())) {
            if (currentDetail.getOrderNumber() > completedDetail.getOrderNumber())
                return progress;

            throw new IllegalStateException(
                    "Bài vừa hoàn thành không phải bài hiện tại của lộ trình!"
            );
        }

        LearningPathDetail nextDetail =
                detailService.getNextDetail(
                        learningPathId,
                        completedDetail.getOrderNumber()
                );

        if (nextDetail != null) {
            progress.setCurrentDetail(nextDetail);

            studentLearningPathRepository.updateStudentLearningPath(progress);

            return progress;
        }

        progress.setCurrentDetail(null);
        progress.setStatus(StudentLearningPath.ProgressStatus.COMPLETED);
        progress.setCompletedAt(LocalDateTime.now());

        studentLearningPathRepository.updateStudentLearningPath(progress);

        return progress;
    }
}