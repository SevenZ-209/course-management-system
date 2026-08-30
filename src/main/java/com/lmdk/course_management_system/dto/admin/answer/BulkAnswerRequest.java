package com.lmdk.course_management_system.dto.admin.answer;

import lombok.Data;

import java.util.List;

@Data
public class BulkAnswerRequest {

    private Integer questionId;

    private List<AnswerItem> answers;


    @Data
    public static class AnswerItem {

        private String content;

        private Boolean correct;

        private Integer orderNumber;

    }
}