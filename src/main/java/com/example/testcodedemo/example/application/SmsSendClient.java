package com.example.testcodedemo.example.application;

public interface SmsSendClient {

    /**
     * SMS 메시지를 전송한다.
     *
     * @param phoneNumber 수신자 전화번호 xxx-xxxx-xxxx
     * @param message 보낼 문자 내용
     * @return SmsSendResult
     */
    SmsSendResult sendSms(String phoneNumber, String message);

}
