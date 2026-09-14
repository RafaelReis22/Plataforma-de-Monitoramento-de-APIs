-- Migration V2: Habilitação do TimescaleDB, Hypertables, Compressão Colunar e Retenção
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'timescaledb') THEN
        PERFORM create_hypertable('metric_records', 'timestamp', if_not_exists => TRUE, migrate_data => TRUE);
        ALTER TABLE metric_records SET (
            timescaledb.compress,
            timescaledb.compress_segmentby = 'tenant_id, servico, nome'
        );
        PERFORM add_compression_policy('metric_records', INTERVAL '7 days', if_not_exists => TRUE);
        PERFORM add_retention_policy('metric_records', INTERVAL '30 days', if_not_exists => TRUE);
    END IF;
END $$;

-- Tabela para rastros OpenTelemetry (Traces) com suporte a Hypertables
CREATE TABLE IF NOT EXISTS trace_records (
    id VARCHAR(64) NOT NULL,
    trace_id VARCHAR(64) NOT NULL,
    span_id VARCHAR(64) NOT NULL,
    parent_span_id VARCHAR(64),
    service_name VARCHAR(100) NOT NULL,
    operation_name VARCHAR(200) NOT NULL,
    duration_ms DOUBLE PRECISION NOT NULL,
    status_code VARCHAR(20) DEFAULT 'OK',
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    tenant_id VARCHAR(36) DEFAULT 'tenant-default',
    tags TEXT,
    PRIMARY KEY (trace_id, span_id, timestamp)
);

CREATE INDEX IF NOT EXISTS idx_trace_records_trace_id ON trace_records(trace_id);
CREATE INDEX IF NOT EXISTS idx_trace_records_service_time ON trace_records(service_name, timestamp);
CREATE INDEX IF NOT EXISTS idx_trace_records_tenant ON trace_records(tenant_id);

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM pg_extension WHERE extname = 'timescaledb') THEN
        PERFORM create_hypertable('trace_records', 'timestamp', if_not_exists => TRUE, migrate_data => TRUE);
        ALTER TABLE trace_records SET (
            timescaledb.compress,
            timescaledb.compress_segmentby = 'tenant_id, service_name'
        );
        PERFORM add_compression_policy('trace_records', INTERVAL '7 days', if_not_exists => TRUE);
        PERFORM add_retention_policy('trace_records', INTERVAL '14 days', if_not_exists => TRUE);
    END IF;
END $$;
