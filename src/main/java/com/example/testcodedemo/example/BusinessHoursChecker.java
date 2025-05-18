package com.example.testcodedemo.example;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class BusinessHoursChecker {

    private final Clock clock;

    private final MemberStatusClient memberStatusClient;

    // 영업시간 여부 확인 (9:00 ~ 18:00 포함)
    public boolean isWithinBusinessHours() {
        LocalTime currentTime = LocalTime.now(clock);

        if (!currentTime.isBefore(LocalTime.of(9, 0)) && !currentTime.isAfter(LocalTime.of(18, 0)))
            return true;

        MemberStatus memberStatus = memberStatusClient.getStatus();
        if (memberStatus.isVip())
            return true;

        return false;
    }
}
