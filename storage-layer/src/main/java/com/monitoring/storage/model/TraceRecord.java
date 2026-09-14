package com.monitoring.storage.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "trace_records", indexes = {
        @Index(name = "idx_trace_id", columnList = "traceId"),
        @Index(name = "idx_service_time", columnList = "serviceName, timestamp"),
        @Index(name = "idx_tenant", columnList = "tenantId")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TraceRecord {

    @Id
    @Column(nullable = false, length = 64)
    private String id;

    @Column(nullable = false, length = 64)
    private String traceId;

    @Column(nullable = false, length = 64)
    private String spanId;

    @Column(length = 64)
    private String parentSpanId;

    @Column(nullable = false, length = 100)
    private String serviceName;

    @Column(nullable = false, length = 200)
    private String operationName;

    @Column(nullable = false)
    private Double durationMs;

    @Column(length = 20)
    private String statusCode;

    @Column(nullable = false)
    private Instant timestamp;

    @Column(length = 36)
    private String tenantId;

    @Column(columnDefinition = "TEXT")
    private String tags;
}
