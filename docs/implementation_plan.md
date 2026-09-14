# Plano de Evolução da Plataforma de Monitoramento de APIs (Padrão Enterprise)

Este documento contém a análise detalhada da **Plataforma de Monitoramento de APIs**, mapeando o progresso das entregas dos microsserviços e o roteiro estratégico para consolidar o sistema como uma plataforma de observabilidade e APM de alto nível (nível Datadog / New Relic).

---

## Status de Entrega do Projeto

### 1. Backend Core & Infraestrutura (Java 21 / Spring Boot 3.3.4):
- **`monitoring-agent`**: Coleta de métricas de hardware (CPU, RAM, Disco, Rede I/O) via OSHI 6.6.3 e exposição no Micrometer/Prometheus.
- **`api-gateway-interceptor`**: Interceptor HTTP com medição de tempo, sanitização de URIs e propagação de cabeçalho W3C `traceparent`.
- **`observability-pipeline`**: Serviço de agregação de métricas com suporte a cache Redis L2 e streaming de alta vazão.
- **`storage-layer`**: Persistência de histórico de telemetria no PostgreSQL 16 e consultas PromQL.
- **`dashboard-service`**: API REST centralizadora para consumo do frontend, autenticação JWT e validação PromQL.

### 2. Segurança & Autenticação IAM (Fase 1 - ✅ CONCLUÍDA):
- **Spring Security & JJWT 0.12.6**: Autenticação Stateless por tokens JWT e suporte a cabeçalho `X-API-KEY`.
- **[AuthController](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/dashboard-service/src/main/java/com/monitoring/dashboard/controller/AuthController.java)**: Login (`/api/v1/auth/login`), registro de tenants e geração de API Keys.

### 3. Aplicação Web Dashboard SPA Interativo (Fase 2 - ✅ CONCLUÍDA):
- **[frontend-dashboard](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/frontend-dashboard)**: Web App SPA em React 18, Vite e TypeScript.
- **Abas**: Hardware Overview, API Health, JVM Internals, Central de Alertas, Synthetic Probes, API Keys & SDKs e Design System.
- **Build**: Bundle compilado integrado ao `dashboard-service` em `src/main/resources/static/`.

### 4. Tracing Distribuído & Correlação de Logs (Fase 3 - ✅ CONCLUÍDA):
- **W3C Trace Context**: Injeção e propagação do cabeçalho `traceparent` (`00-<traceId>-<spanId>-01`) e `X-Trace-ID`.
- **Log Correlation MDC**: Injeção de `traceId` e `spanId` no MDC do SLF4J para rastreamento fim a fim.
- **[TracesLogsView.tsx](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/frontend-dashboard/src/views/TracesLogsView.tsx)**: Aba com Diagrama Waterfall de Spans OpenTelemetry e tabela de logs correlacionados.

### 5. Streaming de Eventos & Resiliência (Fase 4 - ✅ CONCLUÍDA):
- **Redis Streams Ingestion**: Envio assíncrono de eventos para `stream:metrics` via **[StreamEventPublisher](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/observability-pipeline/src/main/java/com/monitoring/pipeline/service/StreamEventPublisher.java)**.
- **Resilience4j Protection**: Proteção com `@CircuitBreaker` e `@RateLimiter`.
- **Dead Letter Queue (DLQ)**: Desvio automático de mensagens malformatadas/com falha para `stream:metrics:dlq`.
- **Batch Processing**: Consumidor assíncrono **[StreamEventConsumer](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/observability-pipeline/src/main/java/com/monitoring/pipeline/service/StreamEventConsumer.java)** e endpoint `POST /api/metricas/batch`.

---

## O que Falta & Roteiro das Próximas Fases

### Fase 5: Notificações Multi-Canal, Agente Java Transparente & Probes Sintéticos (⏳ PRÓXIMO PASSO)
*Objetivo: Alertas ativos em tempo real em múltiplos canais de comunicação e instrumentação sem alteração de código.*

#### [NEW] Módulo de Notificações Multi-Canal
- Notificadores síncronos/assíncronos para **Slack Webhooks**, **Microsoft Teams**, **Discord**, **PagerDuty** e **E-mail (SMTP)**.

#### [NEW] `java-agent` Transparente
- Agente Java baseado em Byte Buddy para instrumentação dinâmica de aplicações Java/Spring/Jakarta via opção JVM `-javaagent:monitoring-agent.jar` sem modificar o código-fonte da aplicação monitorada.

#### [MODIFY] Agendador Sintético de SLA/SLO
- Validador ativo agendado (`@Scheduled`) de certificados SSL/TLS, pings HTTPS com cálculo automático de disponibilidade e orçamento de erros (*Error Budget*).

---

### Fase 6: DevOps Enterprise, Helm Charts, Flyway & Testes de Carga (k6)
*Objetivo: Prontidão para produção em Kubernetes, governança de schema SQL e testes de estresse.*

#### [NEW] [infra/helm/api-monitoring-platform](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/infra/helm)
- Helm Chart parametrizado para deploy dos 5 microsserviços + PostgreSQL + Redis + Prometheus + Grafana em Kubernetes (EKS, GKE, AKS).

#### [NEW] Versionamento SQL com Flyway
- Scripts de migração SQL em `storage-layer/src/main/resources/db/migration/`.

#### [NEW] Load Testing com k6
- Scripts de estresse em `infra/load-testing/` para validação de carga de 1.000 a 50.000 requisições/segundo.

---

## User Review Required

> [!IMPORTANT]
> **Aprovação da Fase 5**:
> Com as Fases 1, 2, 3 e 4 concluídas, recomendamos iniciar a **Fase 5 (Notificações Multi-Canal Slack/Teams/PagerDuty e Java Agent Transparente)**. Por favor, confirme para iniciarmos!

---

## Plan de Verificação

### Testes Automatizados
- Compilação e suíte de testes:
  ```bash
  mvn clean test
  ```
- Testes com Testcontainers:
  ```bash
  cd storage-layer && mvn test
  ```

### Testes Manuais & Dashboard
- Subir ambiente via Docker Compose:
  ```bash
  docker compose up -d
  ```
- Acessar `http://localhost:8085` para navegar entre as 8 abas de observabilidade.
