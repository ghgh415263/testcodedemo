package com.example.testcodedemo.example.ui;

import com.example.testcodedemo.example.application.RequestService;
import com.example.testcodedemo.example.application.ResultDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/requests")
@RequiredArgsConstructor
public class RequestController {

    private final RequestService requestService;

    /**
     * 요청을 등록하고, 성공 시 저장된 요청 ID를 반환합니다.
     *
     * @param requestSaveDto 요청 본문
     * @return 저장된 요청 ID
     */
    @PostMapping
    public ApiResponse<ResultDto> createRequest(@RequestBody RequestSaveDto requestSaveDto) {
        ResultDto result = requestService.doService(requestSaveDto);
        return ApiResponse.success(result);
    }

}
