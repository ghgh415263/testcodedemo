package com.example.testcodedemo.example;

import static org.junit.jupiter.api.Assertions.*;

import java.time.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class BusinessHoursCheckerTest {

    private MemberStatusClient memberStatusClient;
    private Clock fixedClock;
    private BusinessHoursChecker checker;

    private void initBusinessHoursCheckerAtTime(LocalTime time, MemberStatus memberStatus) {
        fixedClock = Clock.fixed(
                LocalDateTime.of(2025, 5, 18, time.getHour(), time.getMinute()).toInstant(ZoneOffset.UTC),
                ZoneOffset.UTC
        );
        memberStatusClient = new StubMemberStatusClient(memberStatus);
        checker = new BusinessHoursChecker(fixedClock, memberStatusClient);
    }

    @Test
    @DisplayName("영업시간 이내 일 경우")
    void 영업시간_내일_경우_true() {
        initBusinessHoursCheckerAtTime(LocalTime.of(10, 0), new NotVipMemberStatus());

        boolean result = checker.isWithinBusinessHours();

        assertTrue(result);
    }

    @Test
    @DisplayName("영업시간 외 일 경우 + vip")
    void 영업시간_외_그리고_VIP_일_경우_true() {
        initBusinessHoursCheckerAtTime(LocalTime.of(20, 0), new VipMemberStatus());

        boolean result = checker.isWithinBusinessHours();

        assertTrue(result);
    }

    @Test
    @DisplayName("영업시간 외 일 경우 + vip아님")
    void 영업시간_외_그리고_VIP_아닐_경우_false() {
        initBusinessHoursCheckerAtTime(LocalTime.of(20, 0), new NotVipMemberStatus());

        boolean result = checker.isWithinBusinessHours();

        assertFalse(result);
    }
}
