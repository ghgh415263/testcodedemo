package com.example.testcodedemo.example;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryRequestRepository implements RequestRepository{

    private final Map<Long, MemberRequest> store = new ConcurrentHashMap<>();

    @Override
    public void save(MemberRequest entity) {
        store.put(entity.getId(), entity);
    }
}
