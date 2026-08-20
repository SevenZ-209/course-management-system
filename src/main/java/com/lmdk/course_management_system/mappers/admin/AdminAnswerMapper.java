package com.lmdk.course_management_system.mappers.admin;

import com.lmdk.course_management_system.dto.admin.answer.AdminAnswerResponse;
import com.lmdk.course_management_system.pojo.Answer;

import org.springframework.stereotype.Component;

@Component
public class AdminAnswerMapper {

    public AdminAnswerResponse toResponse(
            Answer answer
    ) {
        var question = answer.getQuestion();
        var assignment = question.getAssignment();
        var course = assignment.getCourse();

        return new AdminAnswerResponse(
                answer.getId(),

                question.getId(),
                question.getContent(),
                question.getType().name(),

                assignment.getId(),
                assignment.getName(),

                course.getId(),
                course.getName(),

                answer.getContent(),
                answer.getOrderNumber(),
                answer.getCorrect()
        );
    }
}