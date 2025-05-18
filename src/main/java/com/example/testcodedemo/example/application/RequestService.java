package com.example.testcodedemo.example.application;

import com.example.testcodedemo.example.ui.RequestSaveDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final BusinessHoursChecker businessHoursChecker;

    private final RequestRepository requestRepository;

    private final SmsSendClient smsSendClient;

    private final MemberStatusClient memberStatusClient;

    public ResultDto doService(RequestSaveDto requestSaveDto){

        MemberInfo memberInfo = memberStatusClient.getStatus();

        if (!businessHoursChecker.isWithinBusinessHours(memberInfo)) {
            throw new isNotBusinessHoursException();
        }

        Long savedId = requestRepository.save(new MemberRequest(requestSaveDto.getContent()));

        SmsSendResult smsSendResult = smsSendClient.sendSms(memberInfo.phoneNumber(), "요청이 완료되었습니다.");

        return new ResultDto(savedId, smsSendResult);
    }
}
