package com.example.testcodedemo.example.testdouble;

import com.example.testcodedemo.example.application.SmsSendClient;
import com.example.testcodedemo.example.application.SmsSendResult;
import lombok.Getter;

@Getter
public class SpySmsSendClient implements SmsSendClient {
    private boolean shouldSucceed = true; // 기본값: 성공
    private boolean wasCalled = false;

    @Override
    public SmsSendResult sendSms(String phoneNumber, String message) {
        this.wasCalled = true;

        if (shouldSucceed) {
            return SmsSendResult.success("test-message-id");
        } else {
            return SmsSendResult.failure("Simulated SMS failure");
        }
    }

    public void setShouldSucceed(boolean shouldSucceed) {
        this.shouldSucceed = shouldSucceed;
    }
}
