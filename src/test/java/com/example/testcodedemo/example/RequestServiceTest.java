package com.example.testcodedemo.example;

import com.example.testcodedemo.example.application.*;
import com.example.testcodedemo.example.testdouble.MemoryRequestRepository;
import com.example.testcodedemo.example.testdouble.SpySmsSendClient;
import com.example.testcodedemo.example.ui.RequestSaveDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RequestServiceTest {

    private SpySmsSendClient smsSendClient;

    private RequestRepository requestRepository;

    private BusinessHoursChecker businessHoursChecker;

    private MemberStatusClient memberStatusClient;

    private RequestService requestService;

    @BeforeEach
    void setUp() {
        // 밑에 두개는 무조건 모든 경우에 호출됨
        businessHoursChecker = mock(BusinessHoursChecker.class);
        memberStatusClient = mock(MemberStatusClient.class);

        // 호출될 수도 있고 아닐 수도 있음
        smsSendClient = new SpySmsSendClient();
        requestRepository = new MemoryRequestRepository();

        requestService = new RequestService(businessHoursChecker, requestRepository, smsSendClient, memberStatusClient);
    }


    @Test
    void 요청성공_sms발송성공() {
        // given
        when(memberStatusClient.getStatus()).thenReturn(new MemberInfo(MemberGrade.DIAMOND, "444-1234-5678"));
        when(businessHoursChecker.isWithinBusinessHours(any())).thenReturn(true);

        // when
        ResultDto result = requestService.doService(new RequestSaveDto("테스트 내용"));

        // then
        assertTrue(result.smsSendResult().isSuccess());
        assertNotNull(result.savedRequestId(), "savedId는 null이면 안 됩니다.");
    }

    @Test
    void 요청성공_sms발송실패() {
        // given
        smsSendClient.setShouldSucceed(false);
        when(memberStatusClient.getStatus()).thenReturn(new MemberInfo(MemberGrade.DIAMOND, "444-1234-5678"));
        when(businessHoursChecker.isWithinBusinessHours(any())).thenReturn(true);

        // when
        ResultDto result = requestService.doService(new RequestSaveDto("테스트 내용"));

        // then
        assertFalse(result.smsSendResult().isSuccess(), "SMS 전송이 실패해야 합니다.");
        assertNotNull(result.savedRequestId(), "savedId는 null이면 안 됩니다.");
    }

    @Test
    void 요청실패_영업시간아님() {
        // given
        when(memberStatusClient.getStatus()).thenReturn(new MemberInfo(MemberGrade.SILVER, "444-1234-5678"));
        when(businessHoursChecker.isWithinBusinessHours(any())).thenReturn(false);

        // when & then
        assertThrows(isNotBusinessHoursException.class, () -> {
            requestService.doService(new RequestSaveDto("테스트 내용"));
        });
    }

}
