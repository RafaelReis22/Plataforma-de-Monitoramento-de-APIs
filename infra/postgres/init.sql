-- Metadados de APIs monitoradas
CREATE TABLE IF NOT EXISTS monitored_apis (
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    base_url    VARCHAR(1000) NOT NULL,
    description TEXT,
    team        VARCHAR(100),
    slo_latency_ms  INTEGER DEFAULT 2000,
    slo_availability NUMERIC(5,2) DEFAULT 99.90,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    active      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS api_endpoints (
    id          BIGSERIAL PRIMARY KEY,
    api_id      BIGINT NOT NULL REFERENCES monitored_apis(id) ON DELETE CASCADE,
    method      VARCHAR(10) NOT NULL,
    path        VARCHAR(1000) NOT NULL,
    description TEXT,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS alert_events (
    id          BIGSERIAL PRIMARY KEY,
    api_id      BIGINT REFERENCES monitored_apis(id),
    alert_name  VARCHAR(255) NOT NULL,
    severity    VARCHAR(50) NOT NULL,
    message     TEXT,
    started_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resolved_at TIMESTAMPTZ,
    status      VARCHAR(20) NOT NULL DEFAULT 'firing'
);

CREATE INDEX IF NOT EXISTS idx_alert_events_api_id ON alert_events(api_id);
CREATE INDEX IF NOT EXISTS idx_alert_events_status ON alert_events(status);
CREATE INDEX IF NOT EXISTS idx_api_endpoints_api_id ON api_endpoints(api_id);

-- Dados iniciais de exemplo
INSERT INTO monitored_apis (name, base_url, description, team)
VALUES
  ('Example API', 'http://api-interceptor:8082', 'API de exemplo para testes', 'platform'),
  ('Storage API', 'http://storage-layer:8084', 'API de armazenamento e queries históricas', 'platform')
ON CONFLICT DO NOTHING;
