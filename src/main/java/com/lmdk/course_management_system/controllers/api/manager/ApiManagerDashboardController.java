package com.lmdk.course_management_system.controllers.api.manager;

import com.lmdk.course_management_system.dto.manager.dashboard.ManagerDashboardResponse;
import com.lmdk.course_management_system.mappers.manager.ManagerOnlineSessionMapper;
import com.lmdk.course_management_system.mappers.manager.ManagerPaymentTransactionMapper;
import com.lmdk.course_management_system.pojo.Course;
import com.lmdk.course_management_system.pojo.CourseClass;
import com.lmdk.course_management_system.pojo.Enrollment;
import com.lmdk.course_management_system.pojo.PaymentTransaction;
import com.lmdk.course_management_system.pojo.User;
import com.lmdk.course_management_system.services.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/manager/dashboard")
@RequiredArgsConstructor
public class ApiManagerDashboardController {

    private final CourseService courseService;
    private final CourseClassService classService;
    private final UserService userService;
    private final EnrollmentService enrollmentService;
    private final PaymentTransactionService transactionService;
    private final OnlineSessionService sessionService;
    private final ManagerPaymentTransactionMapper paymentMapper;
    private final ManagerOnlineSessionMapper sessionMapper;

    @GetMapping
    public ManagerDashboardResponse getDashboard() {
        long activeCourses = courseService.countCourses(params("status", Course.CourseStatus.ACTIVE.name()));
        long activeClasses = classService.countClasses(params("status", CourseClass.ClassStatus.ACTIVE.name()));
        long activeStudents = userService.countUsers(params(
                "role", User.UserRole.STUDENT.name(),
                "status", User.UserStatus.ACTIVE.name()
        ));
        long activeTeachers = userService.countUsers(params(
                "role", User.UserRole.TEACHER.name(),
                "status", User.UserStatus.ACTIVE.name()
        ));
        long activeEnrollments = enrollmentService.countEnrollments(
                params("status", Enrollment.EnrollmentStatus.ACTIVE.name())
        );
        long pendingEnrollments = enrollmentService.countEnrollments(
                params("status", Enrollment.EnrollmentStatus.PENDING_PAYMENT.name())
        );
        long pendingPayments = transactionService.countTransactions(
                params("status", PaymentTransaction.TransactionStatus.PENDING.name())
        );

        Map<String, String> paymentParams = params("page", "1");
        var recentPayments = transactionService.getTransactions(paymentParams)
                .stream().limit(5).map(paymentMapper::toResponse).toList();

        Map<String, String> sessionParams = params(
                "page", "1",
                "date", LocalDate.now().toString(),
                "sort", "asc"
        );
        var todaySessions = sessionService.getSessions(sessionParams)
                .stream().limit(5).map(sessionMapper::toResponse).toList();

        return new ManagerDashboardResponse(
                activeCourses, activeClasses, activeStudents, activeTeachers,
                activeEnrollments, pendingEnrollments, pendingPayments,
                recentPayments, todaySessions
        );
    }

    private Map<String, String> params(String... values) {
        Map<String, String> params = new HashMap<>();
        for(int i = 0; i + 1 < values.length; i += 2) params.put(values[i], values[i + 1]);
        return params;
    }
}
