package com.lmdk.course_management_system.helpers;

import com.lmdk.course_management_system.pojo.OnlineSession;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OnlineSessionHelper {

    public String getStatus(
            OnlineSession session
    ) {

        LocalDateTime now = LocalDateTime.now();

        if(now.isBefore(session.getStartTime()))
            return "UPCOMING";

        if(session.getEndTime() != null
                && now.isAfter(session.getEndTime()))
            return "ENDED";

        return "ONGOING";
    }
}