package com.lmdk.course_management_system.helpers;

import com.lmdk.course_management_system.exceptions.ForbiddenException;
import com.lmdk.course_management_system.pojo.AssignmentAttempt;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Component
public class AssignmentAttemptHelper {

    public LocalDateTime calculateEndTime(
            AssignmentAttempt attempt
    ) {
        if(attempt.getStartedAt() == null)
            return null;

        Integer durationMinutes =
                attempt.getAssignedAssignment()
                        .getAssignment()
                        .getDurationMinutes();

        LocalDateTime durationEnd =
                durationMinutes == null || durationMinutes <= 0
                        ? null
                        : attempt.getStartedAt()
                          .plusMinutes(durationMinutes);

        LocalDateTime dueAt =
                attempt.getAssignedAssignment().getDueAt();

        if(durationEnd == null)
            return dueAt;

        if(dueAt == null)
            return durationEnd;

        return durationEnd.isBefore(dueAt)
                ? durationEnd
                : dueAt;
    }

    public Long calculateRemainingSeconds(
            AssignmentAttempt attempt
    ) {
        LocalDateTime endTime =
                calculateEndTime(attempt);

        if(endTime == null)
            return null;

        long seconds = Duration.between(
                LocalDateTime.now(),
                endTime
        ).getSeconds();

        return Math.max(seconds, 0);
    }

    public void validateOwner(
            AssignmentAttempt attempt,
            Integer studentId
    ) {
        if(attempt == null)
            throw new IllegalArgumentException(
                    "Lần làm bài không tồn tại!"
            );

        if(!attempt.getAssignedAssignment()
                .getStudent()
                .getId()
                .equals(studentId))
            throw new ForbiddenException(
                    "Bạn không có quyền truy cập bài làm này!"
            );
    }
}