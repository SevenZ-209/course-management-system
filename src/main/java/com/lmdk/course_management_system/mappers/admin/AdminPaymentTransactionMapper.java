package com.lmdk.course_management_system.mappers.admin;

import com.lmdk.course_management_system.dto.admin.payment.AdminPaymentTransactionResponse;
import com.lmdk.course_management_system.pojo.PaymentTransaction;

import org.springframework.stereotype.Component;

@Component
public class AdminPaymentTransactionMapper {

    public AdminPaymentTransactionResponse toResponse(
            PaymentTransaction transaction
    ) {
        var enrollment = transaction.getEnrollment();
        var student = enrollment.getStudent();
        var courseClass = enrollment.getCourseClass();
        var course = courseClass.getCourse();

        return new AdminPaymentTransactionResponse(
                transaction.getId(),

                enrollment.getId(),

                student.getId(),
                student.getFullName(),
                student.getUsername(),

                courseClass.getId(),
                courseClass.getName(),

                course.getId(),
                course.getName(),

                transaction.getAmount(),
                transaction.getPaymentMethod(),
                transaction.getTransactionCode(),
                transaction.getStatus().name(),

                transaction.getPaidAt(),
                transaction.getCreatedAt()
        );
    }
}