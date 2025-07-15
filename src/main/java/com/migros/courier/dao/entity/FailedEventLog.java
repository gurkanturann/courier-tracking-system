package com.migros.courier.dao.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class FailedEventLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventType;

    @Lob
    @Column(nullable = false)
    private String eventPayload;

    @Lob
    private String errorDetails;

    private LocalDateTime failedAt;

    private int retryCount = 0;

    private boolean resolved = false;
}
