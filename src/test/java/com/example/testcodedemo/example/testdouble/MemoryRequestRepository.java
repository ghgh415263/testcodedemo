package com.example.testcodedemo.example.testdouble;

import com.example.testcodedemo.example.application.RequestRepository;
import com.example.testcodedemo.example.application.MemberRequest;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import java.util.concurrent.atomic.AtomicLong;

public class MemoryRequestRepository implements RequestRepository {

    private final Map<Long, MemberRequest> store = new ConcurrentHashMap<>();
    private final AtomicLong sequence = new AtomicLong(1); // 1부터 시작

    @Override
    public Long save(MemberRequest entity) {
        Long newId = sequence.getAndIncrement();
        setIdViaReflection(entity, newId);
        store.put(newId, entity);
        return newId;
    }

    @Override
    public Optional<MemberRequest> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    private void setIdViaReflection(MemberRequest entity, Long id) {
        try {
            Field idField = MemberRequest.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entity, id);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException("Failed to set id via reflection", e);
        }
    }

}
