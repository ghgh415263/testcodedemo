package com.example.testcodedemo.example.application;

import java.util.Optional;

public interface RequestRepository {
    Long save(MemberRequest entity);

    Optional<MemberRequest> findById(Long id);
}
