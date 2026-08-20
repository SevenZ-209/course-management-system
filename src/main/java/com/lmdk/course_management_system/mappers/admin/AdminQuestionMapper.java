package com.lmdk.course_management_system.mappers.admin;

import com.lmdk.course_management_system.dto.admin.question.AdminQuestionResponse;
import com.lmdk.course_management_system.pojo.Question;

import org.springframework.stereotype.Component;

@Component
public class AdminQuestionMapper {

    public AdminQuestionResponse toResponse(
            Question question
    ) {
        var assignment = question.getAssignment();
        var course = assignment.getCourse();

        return new AdminQuestionResponse(
                question.getId(),

                assignment.getId(),
                assignment.getName(),

                course.getId(),
                course.getName(),

                question.getContent(),
                question.getType().name(),
                question.getScore(),
                question.getOrderNumber()
        );
    }
}