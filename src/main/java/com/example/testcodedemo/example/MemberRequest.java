package com.example.testcodedemo.example;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "requests")
public class MemberRequest {

    @Id
    private Long id;

    @Column(name = "content", nullable = false)
    private String content;

    public MemberRequest(String content) {
    }
}
