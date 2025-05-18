package com.example.testcodedemo.example;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberStatus {
    public enum MemberGrade {
        SILVER,
        GOLD,
        PLATINUM,
        DIAMOND
    }

    private MemberGrade grade;

    @Override
    public String toString() {
        return "MemberStatus{grade=" + grade + "}";
    }

    public boolean isVip(){
        return grade == MemberGrade.PLATINUM || grade == MemberGrade.DIAMOND;
    }
}
