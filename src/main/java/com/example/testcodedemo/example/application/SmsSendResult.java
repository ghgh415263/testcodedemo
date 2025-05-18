package com.example.testcodedemo.example.application;

public record SmsSendResult(boolean isSuccess, String messageId, String errorMessage) {
    public static SmsSendResult success(String messageId) {
        return new SmsSendResult(true, messageId, null);
    }

    public static SmsSendResult failure(String errorMessage) {
        return new SmsSendResult(false, null, errorMessage);
    }
}
