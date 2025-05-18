package com.example.testcodedemo.example;

public class StubMemberStatusClient implements MemberStatusClient {

    private final MemberStatus memberStatus;

    public StubMemberStatusClient(MemberStatus memberStatus) {
        this.memberStatus = memberStatus;
    }

    @Override
    public MemberStatus getStatus() {
        return memberStatus;
    }
}
