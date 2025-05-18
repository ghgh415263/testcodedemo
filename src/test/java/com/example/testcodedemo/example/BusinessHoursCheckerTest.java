package com.example.testcodedemo.example;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;

import com.example.testcodedemo.example.application.BusinessHoursChecker;
import com.example.testcodedemo.example.application.MemberGrade;
import com.example.testcodedemo.example.application.MemberInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BusinessHoursCheckerTest {
    private Clock fixedClock;
    private BusinessHoursChecker checker;

    private void initBusinessHoursCheckerAtTime(LocalTime time) {
        fixedClock = Clock.fixed(
                LocalDateTime.of(2025, 5, 18, time.getHour(), time.getMinute()).toInstant(ZoneOffset.UTC),
                ZoneOffset.UTC
        );
        checker = new BusinessHoursChecker(fixedClock);
    }

    @Test
    @DisplayName("영업시간 이내 일 경우")
    void 영업시간_이내() {
        initBusinessHoursCheckerAtTime(LocalTime.of(10, 0));

        boolean result = checker.isWithinBusinessHours(new MemberInfo(MemberGrade.SILVER, "444-4444-4444"));

        assertTrue(result);
    }

    @Test
    @DisplayName("영업시간 외 일 경우 + vip")
    void 영업시간_외_그리고_VIP일_경우() {
        initBusinessHoursCheckerAtTime(LocalTime.of(20, 0));

        boolean result = checker.isWithinBusinessHours(new MemberInfo(MemberGrade.PLATINUM, "444-4444-4444"));

        assertTrue(result);
    }

    @Test
    @DisplayName("영업시간 외 일 경우 + vip아님")
    void 영업시간_외_그리고_VIP아닐_경우() {
        initBusinessHoursCheckerAtTime(LocalTime.of(20, 0));

        boolean result = checker.isWithinBusinessHours(new MemberInfo(MemberGrade.SILVER, "444-4444-4444"));

        assertFalse(result);
    }
}
