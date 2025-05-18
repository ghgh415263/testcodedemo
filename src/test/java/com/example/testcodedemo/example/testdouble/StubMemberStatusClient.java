package com.example.testcodedemo.example.testdouble;

import com.example.testcodedemo.example.application.MemberInfo;
import com.example.testcodedemo.example.application.MemberStatusClient;

public class StubMemberStatusClient implements MemberStatusClient {

    private final MemberInfo memberInfo;

    public StubMemberStatusClient(MemberInfo memberInfo) {
        this.memberInfo = memberInfo;
    }

    @Override
    public MemberInfo getStatus() {
        return memberInfo;
    }
}
