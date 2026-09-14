# Plano de Evolução da Plataforma de Monitoramento de APIs (Padrão Enterprise)

Este documento contém a análise completa da **Plataforma de Monitoramento de APIs**, mapeando os módulos existentes, o progresso já realizado e o roteiro detalhado para elevar o sistema ao nível de uma plataforma enterprise completa (comparável a soluções como Datadog, New Relic e Dynatrace).

---

## Análise do Estado Atual do Projeto

### 1. Módulos do Backend (Java 21 / Spring Boot 3.3.4):
- **`monitoring-agent`**: Coleta de métricas de hardware (CPU, Memória RAM, Disco, Rede I/O) via OSHI 6.6.3 expostas no endpoint `/actuator/prometheus`.
- **`api-gateway-interceptor`**: Interceptor de requisições HTTP com registro de tempo de execução, status code, rotas e cache Redis.
- **`observability-pipeline`**: Serviço de agregação de métricas com fila e cache L2 em Redis.
- **`storage-layer`**: Persistência de histórico de telemetria no PostgreSQL 16 com cliente PromQL.
- **`dashboard-service`**: API REST consolidada para consumo do frontend e validação de expressões PromQL.

### 2. Segurança & Autenticação (Fase 1 - ✅ CONCLUÍDA):
- **Spring Security 6 & JJWT 0.12.6**: Autenticação Stateless por tokens JWT e suporte a cabeçalho `X-API-KEY`.
- **[AuthController](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/dashboard-service/src/main/java/com/monitoring/dashboard/controller/AuthController.java)**: Login (`/api/v1/auth/login`), registro, perfil e gerador de API Keys.
- **[TelemetryController](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/dashboard-service/src/main/java/com/monitoring/dashboard/controller/TelemetryController.java)**: Endpoints REST consolidados de telemetria em tempo real.

### 3. Aplicação Web Dashboard SPA (Fase 2 - ✅ CONCLUÍDA):
- **[frontend-dashboard](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/frontend-dashboard)**: Web App SPA em React 18, Vite e TypeScript.
- **Visualizações**: Overview de Hardware, API Health (RPS, Latências P50/P90/P95/P99), JVM Internals, Central de Alertas, Synthetic Probes, API Keys & SDKs (Spring/Express/FastAPI) e Design System Spec.
- **Build Integrado**: Bundle estático compilado em `dashboard-service/src/main/resources/static/`.

---

## O que Falta & Roteiro das Próximas Fases

### Fase 3: Observabilidade Avançada: Tracing Distribuído & Agregação de Logs
*Objetivo: Correlacionar Métricas, Traces (OTel/Jaeger) e Logs (Grafana Loki) para análise completa de causa raiz.*

#### [MODIFY] [api-gateway-interceptor](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/api-gateway-interceptor)
- Integrar SDK do OpenTelemetry Java / Micrometer Tracing.
- Injetar `traceId` e `spanId` no MDC do SLF4J em cada requisição HTTP tratada.
- Propagar cabeçalhos HTTP standard W3C (`traceparent`, `tracestate`).

#### [MODIFY] [infra/docker-compose.yml](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/infra/docker-compose.yml) & [infra/kubernetes/](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/infra/kubernetes)
- Adicionar serviços:
  - **Jaeger / OpenTelemetry Collector**: Recebimento e exportação de traces OTLP.
  - **Grafana Loki & Promtail**: Coleta e indexação de logs estruturados em JSON.

#### [MODIFY] [dashboard-service](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/dashboard-service)
- Adicionar endpoints para consulta de Spans e Logs correlacionados por `traceId`.

---

### Fase 4: Pipeline de Streaming de Alta Vazão & Resiliência
*Objetivo: Suportar milhões de eventos de telemetria por minuto com tolerância a falhas.*

#### [MODIFY] [observability-pipeline](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/observability-pipeline)
- Implementar produtor/consumidor com **Apache Kafka** (ou **Redis Streams** como alternativa leve).
- Adicionar suporte a processamento em lote (batching), compressão de payloads e Dead Letter Queue (DLQ).
- Configurar circuitos de proteção Resilience4j (Circuit Breaker, Rate Limiter e Bulkhead).

---

### Fase 5: Engine de Alertas Multi-Canal, Synthetic Probes & Agente Java Transparente
*Objetivo: Notificação proativa de incidentes e fácil adoção sem alteração de código.*

#### [NEW] Notificadores Multi-Canal
- Integradores nativos para **Slack Webhooks**, **Microsoft Teams**, **Discord**, **PagerDuty** e **E-mail (SMTP)**.

#### [NEW] `java-agent` Transparente
- Agente Java baseado em Byte Buddy para instrumentação transparente de aplicações Spring/Jakarta sem necessidade de alteração no código fonte.

#### [MODIFY] Probes Sintéticos Automáticos
- Agendador `@Scheduled` para disparo de testes HTTP/HTTPS, validação de certificados SSL e cálculo contínuo de Error Budget/SLO.

---

### Fase 6: DevOps Enterprise, Helm Charts, Flyway & Testes de Carga (k6)
*Objetivo: Preparação final para produção, governança de banco de dados e testes de estresse.*

#### [NEW] [infra/helm/api-monitoring-platform](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/infra/helm)
- Helm Chart parametrizado para deploy dos microsserviços + PostgreSQL + Redis + Prometheus + Grafana em Kubernetes.

#### [NEW] Migrações com Flyway
- Versionamento SQL de schema em `storage-layer/src/main/resources/db/migration/`.

#### [NEW] Scripts de Load Testing (k6)
- Scripts de estresse para validação de carga de 1.000 a 50.000 req/s.

---

## User Review Required

> [!IMPORTANT]
> **Aprovação do Roteiro**:
> As Fases 1 e 2 já foram totalmente entregues. Confirme se deseja iniciar o desenvolvimento da **Fase 3 (Tracing Distribuído com OpenTelemetry & Loki)** ou priorizar a **Fase 4 (Pipeline com Kafka)**.

---

## Plan de Verificação

### Testes Automatizados
- Compilação e execução de testes:
  ```bash
  mvn clean test
  ```
- Testes E2E e de integração com Testcontainers:
  ```bash
  cd storage-layer && mvn test
  ```

### Testes Manuais & Interface Web
- Execução local via Docker Compose:
  ```bash
  docker compose up -d
  ```
- Acesso à interface SPA em `http://localhost:8085` para navegação entre as 7 abas de monitoramento.
