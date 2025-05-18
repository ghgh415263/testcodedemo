package com.example.testcodedemo.example.application;

public record MemberInfo(MemberGrade grade, String phoneNumber) {

    public boolean isVip() {
        return grade == MemberGrade.PLATINUM || grade == MemberGrade.DIAMOND;
    }

    @Override
    public String toString() {
        return "MemberStatus{grade=" + grade + "}";
    }
}
