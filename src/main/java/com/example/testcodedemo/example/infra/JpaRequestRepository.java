package com.example.testcodedemo.example.infra;

import com.example.testcodedemo.example.application.MemberRequest;
import com.example.testcodedemo.example.application.RequestRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class JpaRequestRepository implements RequestRepository {

    private final EntityManager em;

    @Override
    public Long save(MemberRequest entity) {
        em.persist(entity);
        return entity.getId();
    }

    @Override
    public Optional<MemberRequest> findById(Long id) {
        return Optional.ofNullable(em.find(MemberRequest.class, id));
    }
}
