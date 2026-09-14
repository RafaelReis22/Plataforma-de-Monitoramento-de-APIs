# Plano de Evolução da Plataforma de Monitoramento de APIs (Padrão Enterprise)

Este documento contém a análise completa da **Plataforma de Monitoramento de APIs**, mapeando a arquitetura dos microsserviços, os módulos entregues até o momento e o roteiro para transformar o sistema em uma solução de APM e observabilidade enterprise de alto nível (nível Datadog / New Relic).

---

## Status de Entrega do Projeto

### 1. Backend Core & Infraestrutura (Java 21 / Spring Boot 3.3.4):
- **`monitoring-agent`**: Coleta de hardware (CPU, RAM, Disco, Rede) via OSHI 6.6.3 e exposição Micrometer/Prometheus.
- **`api-gateway-interceptor`**: Interceptor HTTP com medição de tempo, status code, sanitize de URIs e propagação de cabeçalho W3C `traceparent`.
- **`observability-pipeline`**: Agregação de métricas com suporte a cache Redis L2.
- **`storage-layer`**: Persistência de histórico de telemetria no PostgreSQL 16 e consultas PromQL.
- **`dashboard-service`**: API REST centralizadora para consumo do frontend, autenticação JWT e validação PromQL.

### 2. Segurança & Autenticação IAM (Fase 1 - ✅ CONCLUÍDA):
- **Spring Security & JJWT 0.12.6**: Autenticação Stateless por tokens JWT e suporte a cabeçalho `X-API-KEY`.
- **[AuthController](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/dashboard-service/src/main/java/com/monitoring/dashboard/controller/AuthController.java)**: Login (`/api/v1/auth/login`), registro de tenants e geração de API Keys.

### 3. Aplicação Web Dashboard SPA Interativo (Fase 2 - ✅ CONCLUÍDA):
- **[frontend-dashboard](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/frontend-dashboard)**: Aplicação Web SPA desenvolvida em React 18, Vite e TypeScript.
- **Abas**: Hardware Overview, API Health, JVM Internals, Central de Alertas, Synthetic Probes, API Keys & SDKs e Design System.
- **Build**: Bundle compilado integrado ao `dashboard-service` em `src/main/resources/static/`.

### 4. Tracing Distribuído & Correlação de Logs (Fase 3 - ✅ CONCLUÍDA):
- **W3C Trace Context**: Injeção e propagação de `traceparent` (`00-<traceId>-<spanId>-01`) e cabeçalho `X-Trace-ID`.
- **Log Correlation MDC**: Injeção de `traceId` e `spanId` no MDC do SLF4J para rastreamento fim a fim.
- **[TracesLogsView.tsx](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/frontend-dashboard/src/views/TracesLogsView.tsx)**: Aba com Diagrama Waterfall de Spans OpenTelemetry e tabela de logs correlacionados.

---

## O que Falta & Roteiro das Próximas Fases

### Fase 4: Pipeline de Streaming de Alta Vazão & Resiliência (⏳ PRÓXIMO PASSO)
*Objetivo: Processar milhões de eventos por minuto com desacoplamento total entre produtores e consumidores.*

#### [MODIFY] [observability-pipeline](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/observability-pipeline)
- Implementar consumidor e produtor assíncrono com **Apache Kafka** (ou **Redis Streams** como broker leve).
- Adicionar suporte a processamento em lote (*batching*), compressão de payloads e Dead Letter Queue (DLQ) para dados corrompidos.
- Configurar circuitos de resiliência com Resilience4j (*Circuit Breaker*, *Rate Limiter* e *Bulkhead*).

---

### Fase 5: Engine de Alertas Multi-Canal, Synthetic Probes & Agente Java Transparente
*Objetivo: Alertas em tempo real e instrumentação sem alteração de código.*

#### [NEW] Notificadores Multi-Canal
- Módulo de notificação nativo com suporte a **Slack Webhooks**, **Microsoft Teams**, **Discord**, **PagerDuty** e **E-mail (SMTP)**.

#### [NEW] `java-agent` Transparente
- Agente Java baseado em Byte Buddy para instrumentação dinâmica de aplicações Spring Boot/Jakarta via `-javaagent:monitoring-agent.jar`.

#### [MODIFY] Agendador Sintético de SLA/SLO
- Monitoramento ativo agendado (`@Scheduled`) para validação de certificados SSL/TLS, pings HTTPS e orçamento de erros (*Error Budget*).

---

### Fase 6: DevOps Enterprise, Helm Charts, Flyway & Testes de Carga (k6)
*Objetivo: Prontidão para produção, deploys reproduzíveis e testes de estresse.*

#### [NEW] [infra/helm/api-monitoring-platform](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/infra/helm)
- Helm Chart parametrizado para deploy dos 5 microsserviços + PostgreSQL + Redis + Prometheus + Grafana em clusters Kubernetes.

#### [NEW] Versionamento SQL com Flyway
- Migrações idempotentes em `storage-layer/src/main/resources/db/migration/`.

#### [NEW] Load Testing com k6
- Scripts k6 para simulação de carga de 1.000 a 50.000 requisições/segundo.

---

## User Review Required

> [!IMPORTANT]
> **Aprovação dos Próximos Passos**:
> Com a conclusão das Fases 1, 2 e 3, recomendamos iniciar a **Fase 4 (Pipeline de Streaming com Kafka/Redis Streams e Resilience4j)**. Por favor, confirme para prosseguirmos!

---

## Plan de Verificação

### Testes Automatizados
- Executar compilação e suíte de testes:
  ```bash
  mvn clean test
  ```
- Executar testes com Testcontainers:
  ```bash
  cd storage-layer && mvn test
  ```

### Testes Manuais & Dashboard
- Subir infraestrutura local:
  ```bash
  docker compose up -d
  ```
- Acessar `http://localhost:8085` para navegar entre as 8 abas de observabilidade (incluindo Traces & Logs).
