package com.lmdk.course_management_system.services.impl;

import com.lmdk.course_management_system.dto.student.assignment.CourseAssignmentResponse;
import com.lmdk.course_management_system.helpers.AssignedAssignmentHelper;
import com.lmdk.course_management_system.pojo.*;
import com.lmdk.course_management_system.repository.*;
import com.lmdk.course_management_system.services.*;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentLearningPathServiceImpl implements StudentLearningPathService {

    private final StudentLearningPathRepository studentLearningPathRepository;
    private final LearningPathService learningPathService;
    private final LearningPathDetailService detailService;
    private final EnrollmentService enrollmentService;
    private final UserService userService;
    private final LearningPathDetailRepository learningPathDetailRepository;
    private final LearningPathRepository learningPathRepository;
    private final AssignedAssignmentRepository assignedAssignmentRepository;
    private final AssignmentAttemptRepository assignmentAttemptRepository;
    private final AssignedAssignmentHelper assignedAssignmentHelper;

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

        StudentLearningPath saved =
                studentLearningPathRepository
                        .addStudentLearningPath(studentLearningPath);

        assignAllLearningPathAssignments(saved);

        return saved;
    }

    private void assignAllLearningPathAssignments(
            StudentLearningPath progress
    ) {

        List<LearningPathDetail> details =
                detailService.getDetailsByLearningPath(
                        progress.getLearningPath().getId()
                );

        for(LearningPathDetail detail : details){

            if(detail.getAssignment() == null)
                continue;

            if(assignedAssignmentRepository
                    .existsByStudentAndLearningPathDetail(
                            progress.getStudent().getId(),
                            detail.getId()
                    ))
                continue;


            AssignedAssignment assigned =
                    new AssignedAssignment();

            assigned.setStudent(
                    progress.getStudent()
            );

            assigned.setAssignment(
                    detail.getAssignment()
            );

            assigned.setLearningPathDetail(
                    detail
            );

            assigned.setStatus(
                    detail.getOrderNumber() == 1
                            ? AssignedAssignment.AssignedStatus.AVAILABLE
                            : AssignedAssignment.AssignedStatus.LOCKED
            );

            assigned.setAssignedAt(
                    LocalDateTime.now()
            );

            assignedAssignmentRepository
                    .addAssignedAssignment(assigned);
        }
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
    public Assignment getCurrentAssignment(Integer studentId, Integer courseId) {

        StudentLearningPath progress =
                getStudentLearningPathsByStudent(studentId)
                        .stream()
                        .filter(p ->
                                p.getLearningPath()
                                        .getCourse()
                                        .getId()
                                        .equals(courseId)
                        )
                        .findFirst()
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Không tìm thấy lộ trình học!"
                                )
                        );

        if(progress.getCurrentDetail() == null)
            return null;

        return progress
                .getCurrentDetail()
                .getAssignment();
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
    public List<StudentLearningPath> getStudentLearningPathsByStudentsAndCourse(
            List<Integer> studentIds, Integer courseId) {
        return studentLearningPathRepository
                .getStudentLearningPathsByStudentsAndCourse(studentIds, courseId);
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
                studentLearningPathRepository.getStudentLearningPathForUpdate(
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

        if (nextDetail == null) {

            List<LearningPathDetail> allDetails =
                    detailService.getDetailsByLearningPath(
                            learningPathId
                    );

            nextDetail = allDetails.stream()
                    .filter(detail ->
                            detail.getOrderNumber()
                                    > completedDetail.getOrderNumber()
                    )
                    .findFirst()
                    .orElse(null);
        }

        if (nextDetail != null) {
            progress.setCurrentDetail(nextDetail);

            AssignedAssignment nextAssigned =
                    assignedAssignmentRepository
                            .getByStudentAndLearningPathDetail(
                                    studentId,
                                    nextDetail.getId()
                            );

            if(nextAssigned != null){

                nextAssigned.setStatus(
                        AssignedAssignment.AssignedStatus.AVAILABLE
                );

                assignedAssignmentRepository
                        .updateAssignedAssignment(nextAssigned);
            }

            studentLearningPathRepository.updateStudentLearningPath(progress);

            return progress;
        }

        progress.setCurrentDetail(null);
        progress.setStatus(StudentLearningPath.ProgressStatus.COMPLETED);
        progress.setCompletedAt(LocalDateTime.now());

        studentLearningPathRepository.updateStudentLearningPath(progress);


        return progress;
    }

    @Override
    public void createStudentLearningPath(User student, Course course) {

        if(student == null)
            throw new IllegalArgumentException("Học viên không tồn tại!");

        if(course == null)
            throw new IllegalArgumentException("Khóa học không tồn tại!");

        List<LearningPath> paths =
                learningPathService.getLearningPathsByCourse(course.getId());

        if(paths.isEmpty())
            return;

        LearningPath path = paths.get(0);

        if(studentLearningPathRepository.existsStudentLearningPath(
                student.getId(),
                path.getId()
        ))
            return;

        List<LearningPathDetail> details =
                detailService.getDetailsByLearningPath(path.getId());

        if(details.isEmpty())
            return;

        StudentLearningPath progress =
                new StudentLearningPath();

        progress.setStudent(student);
        progress.setLearningPath(path);
        progress.setStatus(
                StudentLearningPath.ProgressStatus.IN_PROGRESS
        );
        progress.setStartedAt(LocalDateTime.now());

        details.forEach(d ->
                System.out.println(
                        "DETAIL: "
                                + d.getId()
                                + " ORDER:"
                                + d.getOrderNumber()
                )
        );

        LearningPathDetail firstDetail = details
                .stream()
                .min((a,b) ->
                        a.getOrderNumber()
                                .compareTo(b.getOrderNumber())
                )
                .orElseThrow();

        progress.setCurrentDetail(firstDetail);

        StudentLearningPath saved =
                studentLearningPathRepository.addStudentLearningPath(progress);

        assignAllLearningPathAssignments(saved.getId());
    }

    private void assignAllLearningPathAssignments(
            Integer studentLearningPathId
    ) {

        StudentLearningPath progress =
                studentLearningPathRepository
                        .getStudentLearningPathById(
                                studentLearningPathId
                        );

        List<LearningPathDetail> details =
                detailService.getDetailsByLearningPath(
                        progress.getLearningPath().getId()
                );
        System.out.println(
                "TOTAL DETAIL = " + details.size()
        );
        for(LearningPathDetail detail : details){

            System.out.println(
                    "CREATE ASSIGNMENT: "
                            + detail.getId()
                            + " - "
                            + detail.getAssignment().getId());

            if(detail.getAssignment() == null)
                continue;

            if(assignedAssignmentRepository
                    .existsByStudentAndLearningPathDetail(
                            progress.getStudent().getId(),
                            detail.getId()
                    ))
                continue;


            AssignedAssignment assigned =
                    new AssignedAssignment();

            assigned.setStudent(
                    progress.getStudent()
            );

            assigned.setAssignment(
                    detail.getAssignment()
            );

            assigned.setLearningPathDetail(
                    detail
            );

            assigned.setStatus(
                    detail.getOrderNumber() == 1
                            ? AssignedAssignment.AssignedStatus.AVAILABLE
                            : AssignedAssignment.AssignedStatus.LOCKED
            );

            assigned.setAssignedAt(
                    LocalDateTime.now()
            );


            assignedAssignmentRepository
                    .addAssignedAssignment(assigned);
        }
    }

    @Override
    public boolean canAccessLesson(Integer studentId, Integer lessonId) {

        List<StudentLearningPath> paths =
                getStudentLearningPathsByStudent(studentId);

        for(StudentLearningPath path : paths){

            List<LearningPathDetail> details =
                    detailService.getDetailsByLearningPath(
                            path.getLearningPath().getId()
                    );

            for(LearningPathDetail detail : details){

                if(detail.getAssignment() == null)
                    continue;

                Lesson lesson =
                        detail.getAssignment()
                                .getLesson();

                if(lesson != null &&
                        lesson.getId().equals(lessonId))
                    return true;
            }
        }

        return false;
    }

    @Override
    public List<CourseAssignmentResponse> getCourseAssignments(
            Integer studentId,
            Integer courseId
    ) {

        List<AssignedAssignment> assignments =
                assignedAssignmentRepository
                        .getByStudentAndCourse(
                                studentId,
                                courseId
                        );


        Map<Integer, AssignmentAttempt> latestAttempts = assignmentAttemptRepository
                .getLatestAttemptsByAssignedAssignmentIds(
                        assignments.stream().map(AssignedAssignment::getId).toList()
                )
                .stream()
                .collect(Collectors.toMap(
                        attempt -> attempt.getAssignedAssignment().getId(),
                        Function.identity()
                ));

        return assignments.stream()
                .map(assigned -> {

                    AssignmentAttempt attempt = latestAttempts.get(assigned.getId());


                    Integer attemptId =
                            attempt == null
                                    ? null
                                    : attempt.getId();

                    Integer latestAttemptNumber =
                            attempt == null
                                    ? null
                                    : attempt.getAttemptNumber();

                    String latestAttemptStatus =
                            attempt == null
                                    ? null
                                    : attempt.getStatus().name();

                    boolean canStart = assignedAssignmentHelper
                            .canStart(assigned, attempt);


                    Integer orderNumber = null;


                    if (assigned.getLearningPathDetail() != null) {

                        orderNumber =
                                assigned.getLearningPathDetail()
                                        .getOrderNumber();

                    }


                    String status;

                    if (assigned.getStatus()
                            == AssignedAssignment.AssignedStatus.COMPLETED) {

                        status = "COMPLETED";

                    } else if (assigned.getStatus()
                            == AssignedAssignment.AssignedStatus.LOCKED) {

                        status = "LOCKED";

                    } else {

                        status = "AVAILABLE";
                    }


                    return new CourseAssignmentResponse(
                            assigned.getId(),
                            assigned.getAssignment().getId(),
                            assigned.getAssignment().getName(),
                            orderNumber,
                            status,
                            attemptId,
                            latestAttemptNumber,
                            latestAttemptStatus,
                            canStart,
                            assigned.getAssignment().getType().name(),
                            assigned.getAssignment().getDurationMinutes()
                    );

                })
                .toList();
    }
}