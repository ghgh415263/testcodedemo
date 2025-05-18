package com.example.testcodedemo.example;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final BusinessHoursChecker businessHoursChecker;

    private final RequestRepository requestRepository;

    public void doService(RequestSaveDto requestSaveDto){
        if (!businessHoursChecker.isWithinBusinessHours()) {
            throw new isNotBusinessHoursException();
        }
        requestRepository.save(new MemberRequest(requestSaveDto.getContent()));
    }
}
