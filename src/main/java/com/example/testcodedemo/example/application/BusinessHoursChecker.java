package com.example.testcodedemo.example.application;

import com.example.testcodedemo.example.application.MemberInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalTime;

@Component
@RequiredArgsConstructor
public class BusinessHoursChecker {

    private final Clock clock;

    /**
     * 회원이 업무 시간 내에 있는지 여부를 판단한다.
     * 업무 시간은 오전 9시부터 오후 6시까지로 정의됨.
     * 단, VIP 회원인 경우에는 업무 시간 외에도 true 반환.
     *
     * @param memberInfo 회원 정보 객체
     * @return 업무 시간 내이거나 VIP 회원이면 true, 아니면 false
     */
    public boolean isWithinBusinessHours(MemberInfo memberInfo) {
        LocalTime currentTime = LocalTime.now(clock);

        if (!currentTime.isBefore(LocalTime.of(9, 0)) && !currentTime.isAfter(LocalTime.of(18, 0)))
            return true;

        if (memberInfo.isVip())
            return true;

        return false;
    }
}
