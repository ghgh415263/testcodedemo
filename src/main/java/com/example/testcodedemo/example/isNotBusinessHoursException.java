package com.example.testcodedemo.example;

public class isNotBusinessHoursException extends RuntimeException {

    public isNotBusinessHoursException() {
        super("현재는 영업시간이 아닙니다. 요청을 처리할 수 없습니다.");
    }

    public isNotBusinessHoursException(String message) {
        super(message);
    }
}
