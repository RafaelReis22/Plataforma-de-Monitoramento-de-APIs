package com.monitoring.storage.repository;

import com.monitoring.storage.model.TraceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraceRecordRepository extends JpaRepository<TraceRecord, String> {

    List<TraceRecord> findByTraceId(String traceId);

    List<TraceRecord> findByServiceNameOrderByTimestampDesc(String serviceName);

    @Query("SELECT t FROM TraceRecord t WHERE (:tenantId IS NULL OR t.tenantId = :tenantId) ORDER BY t.timestamp DESC")
    List<TraceRecord> findRecentTraces(@Param("tenantId") String tenantId);
}
