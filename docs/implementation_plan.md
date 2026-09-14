# Plano de Evolução da Plataforma de Monitoramento de APIs (Padrão Enterprise)

Este documento contém a análise completa da arquitetura atual da **Plataforma de Monitoramento de APIs** e estabelece um **Plano em Fases** detalhado para evoluir o sistema até o nível de uma plataforma enterprise completa (comparável a soluções como Datadog, New Relic e Dynatrace).

---

## Análise do Estado Atual

### O que já está implementado:
1. **`monitoring-agent`**: Agente em Spring Boot 3.3 / Java 21 que coleta métricas de hardware (CPU, Memória, Disco, Rede) via OSHI 6.6.3 e expõe endpoints Micrometer/Prometheus.
2. **`api-gateway-interceptor`**: Interceptor HTTP Spring MVC que registra tempo de resposta, códigos de status e detalhes de endpoints, integrado com cache Redis.
3. **`observability-pipeline`**: Serviço de agregação de métricas com fila e cache em Redis.
4. **`storage-layer`**: Camada de persistência histórica usando PostgreSQL 16 e cliente de consulta PromQL.
5. **`dashboard-service`**: API REST que serve endpoints consolidados de métricas e status para visualização.
6. **Infraestrutura & Dashboards**:
   - `docker-compose.yml` pré-configurado com PostgreSQL, Redis, Prometheus e Grafana.
   - 4 Dashboards Grafana JSON em `infra/grafana/dashboards/`.
   - Regras de alerta básicas no Prometheus (`infra/prometheus/alerts.yml`).
   - Manifestos Kubernetes (Deployments, Services, Ingress, ConfigMaps).
   - Protótipos visuais estáticos HTML/CSS em `docs/design/`.
   - Workflow de CI/CD de segurança em `.github/workflows/security-scan.yml`.

---

## O que Falta & Oportunidades de Melhoria (Gaps)

Para atingir o padrão **Enterprise / Alto Nível**, faltam as seguintes capacidades estratégicas:

1. **Interface Web Interativa (Frontend SPA)**:
   - Atualmente existem apenas protótipos HTML estáticos e dashboards Grafana externos. Falta uma aplicação Web (SPA React/Vite ou Next.js) integrada com WebSockets/SSE para atualização em tempo real, navegação por rotas de APIs, gerenciamento de alertas e controle de tenants.
2. **Segurança, Autenticação e Multi-Tenancy (IAM)**:
   - Os microsserviços Java atuais não possuem Spring Security nem autenticação. É necessário implementar OAuth2/JWT, controle de acesso baseado em papéis (RBAC) e isolamento por Tenant/Workspace.
3. **Tracing Distribuído & APM (OpenTelemetry / Jaeger)**:
   - O sistema atual coleta apenas métricas agregadas (contador, gauge, tempo). Falta contexto de Trace Distribuído (W3C `traceparent`), propagação de headers, mapa de dependências de microsserviços e visualização de spans.
4. **Agregação e Correlação de Logs (Loki / ELK)**:
   - Não há centralização de logs nem correlação direta entre Logs, Métricas e Traces (TraceId no MDC do SLF4J).
5. **Pipeline de Ingestão de Alta Vazão (Event Streaming)**:
   - O pipeline atual utiliza chamadas HTTP síncronas e Redis simples. Para suportar milhões de requisições por minuto, é preciso adicionar um broker de eventos (Apache Kafka ou Redis Streams) com mecanismos de retentativa, DLQ e backpressure.
6. **Engine de Alertas Nativos & Monitoramento Sintético (Probes/SLA)**:
   - Monitoramento ativo de saúde (HTTP Ping, expiração de certificados SSL, asserção de payload), cálculo dinâmico de SLO/Error Budget e envio direto de notificações para Slack, Teams, PagerDuty, Webhooks e E-mail.
7. **SDKs e Interceptores Multi-Linguagem**:
   - Atualmente o interceptor é apenas um `HandlerInterceptor` Spring Boot. É necessário disponibilizar um Java Agent transparente (Byte Buddy) e SDKs para Node.js (Express), Python (FastAPI/Flask) e proxies (Envoy/Kong/NGINX).
8. **DevOps Enterprise, Helm, Migrações & Load Testing**:
   - Helm Charts para deploy em 1 comando, Flyway/Liquibase para versionamento de schema do banco, scripts de teste de carga (k6) e documentação Swagger/OpenAPI interativa em todos os serviços.

---

## User Review Required

> [!IMPORTANT]
> **Decisões de Arquitetura para Validação**:
> 1. **Tecnologia do Frontend Web**: Recomendamos React 18 + Vite + Tailwind/CSS Modules com Shadcn/UI para criar a aplicação SPA de alta performance integrada aos protótipos de `docs/design/`.
> 2. **Event Streaming Pipeline**: Utilizar **Kafka** (ou **Redis Streams** como alternativa leve) para a mensageria de métricas e traces em tempo real.
> 3. **OpenTelemetry Compliance**: Adotar o padrão padrão da indústria OpenTelemetry (OTel) para métricas e rastreamento distribuído.

---

## Open Questions

> [!NOTE]
> 1. Você deseja que comecemos pela **Fase 1 (Segurança & Autenticação JWT)** ou pela **Fase 2 (Desenvolvimento do Frontend Web Dashboard)**?
> 2. Para a mensageria de streaming, prefere manter o foco em **Redis Streams** (menos recursos de infra infraestrutura) ou avançar para **Apache Kafka**?

---

## Proposed Changes (Plano em Fases)

---

### Fase 1: Autenticação, Segurança & Multi-Tenancy (IAM)
*Objetivo: Proteger todos os serviços e adicionar gerenciamento de usuários, organizações e chaves de API.*

#### [MODIFY] [pom.xml](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/pom.xml)
- Adicionar dependências centralizadas de `spring-boot-starter-security`, `jjwt` (Java JWT) e `spring-security-test`.

#### [NEW] `common-security-module` ou Pacote de Segurança
- Criar módulo/componente reutilizável com:
  - `JwtAuthenticationFilter`: Validação de tokens JWT nas requisições REST.
  - `JwtTokenProvider`: Geração e parsing de tokens com reivindicações de roles e tenant_id.
  - `ApiKeyAuthenticationFilter`: Autenticação de ingestão via chaves de API (`X-API-KEY`) para agentes e interceptores.
  - `SecurityConfig`: Configuração do Spring Security habilitando stateless sessions, CORS e rotas protegidas.

#### [MODIFY] [storage-layer](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/storage-layer) & [dashboard-service](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/dashboard-service)
- Atualizar entidades e tabelas no PostgreSQL para incluir `organizations`, `users`, `api_keys` e campo `tenant_id` em todas as tabelas de métricas para isolamento multi-tenant.

---

### Fase 2: Aplicação Web Frontend Interativa (Dashboard SPA)
*Objetivo: Transformar os protótipos estáticos de `docs/design/` em um Web App completo, dinâmico e responsivo.*

#### [NEW] [frontend-dashboard](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/frontend-dashboard)
- Criar aplicação Web SPA moderna (React + Vite + TypeScript) na raiz do projeto:
  - **Design System & Estilização**: Baseado no protótipo `05-design-system.html` (Dark mode elegante, glassmorphism, gradientes e gráficos com Recharts/Chart.js).
  - **Tela 1: Overview de Hardware** (`/hardware`): Gráficos em tempo real de uso de CPU, Memória, Disco e Rede consumindo a API do `monitoring-agent` / `dashboard-service`.
  - **Tela 2: Saúde das APIs** (`/apis`): Visualização por endpoint, throughput (RPS), tempo de resposta (P50, P90, P95, P99) e taxas de erro (4xx, 5xx).
  - **Tela 3: JVM Internals** (`/jvm`): Leitura de Heap, Non-Heap, GC pauses, contagem de threads e uso de memória por área.
  - **Tela 4: Central de Alertas** (`/alerts`): Lista de alertas ativos, histórico de incidentes, silenciamento e configuração de regras.
  - **Integração WebSockets / SSE**: Atualização contínua das métricas em tempo real sem necessidade de polling constante.

---

### Fase 3: Observabilidade Avançada: Tracing Distribuído & Agregação de Logs
*Objetivo: Conectar Métricas, Traces (OTel/Jaeger) e Logs (Loki) para diagnóstico completo de causa raiz.*

#### [MODIFY] [api-gateway-interceptor](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/api-gateway-interceptor)
- Integrar SDK do OpenTelemetry Java / Micrometer Tracing.
- Injetar `traceId` e `spanId` no MDC do SLF4J em cada requisição HTTP tratada.
- Propagar cabeçalhos HTTP standard W3C (`traceparent`, `tracestate`).

#### [MODIFY] [infra/docker-compose.yml](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/infra/docker-compose.yml) & [infra/kubernetes/](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/infra/kubernetes)
- Adicionar serviços:
  - **Jaeger / OpenTelemetry Collector**: Recebimento e exportação de traces OTLP.
  - **Grafana Loki & Promtail**: Coleta e indexação de logs estruturados em JSON dos containers.

#### [MODIFY] [dashboard-service](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/dashboard-service)
- Adicionar endpoints para consulta de Traces e consulta de Logs correlacionados por `traceId`.

---

### Fase 4: Pipeline de Streaming de Alta Vazão & Resiliência
*Objetivo: Suportar cargas massivas de telemetria sem perda de dados.*

#### [MODIFY] [observability-pipeline](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/observability-pipeline)
- Implementar produtor/consumidor com **Redis Streams** ou **Apache Kafka**.
- Adicionar suporte a processamento em lote (batching), compressão de payloads e Dead Letter Queue (DLQ) para dados malformatados.
- Configurar circuitos de proteção Resilience4j (Circuit Breaker, Rate Limiter e Bulkhead) para chamadas aos repositórios.

---

### Fase 5: Engine de Alertas, Synthetic Probes & SDKs Multi-Linguagem
*Objetivo: Monitoramento ativo proativo e extensibilidade para diferentes tecnologias.*

#### [NEW] [synthetic-monitoring-service] ou Pacote em `dashboard-service`
- **Probes Sintéticos**: Agendador Spring (`@Scheduled`) que dispara pings HTTP/HTTPS periódicos em APIs cadastradas.
- **Validações**: Código de status esperado, tempo limite de resposta, verificação de certificado SSL/TLS e asserção de JSON no corpo.
- **Cálculo de SLA/SLO**: Cálculo contínuo da disponibilidade percentual (ex: 99.9%) e Error Budget restante.

#### [NEW] Notificações Multi-Canal
- Integradores para **Slack Webhooks**, **Microsoft Teams**, **Discord**, **PagerDuty** e **E-mail (SMTP)** disparados automaticamente na violação de regras.

#### [NEW] SDKs / Agente Transparente
- **`java-agent`**: Agente Java baseado em Byte Buddy que monitora qualquer aplicação Spring Boot / Jakarta sem necessidade de alterar código.
- **Node.js / Express Interceptor**: Pacote npm leve para envio de telemetria.
- **Python FastAPI Interceptor**: Middleware ASGI para instrumentação automática em Python.

---

### Fase 6: Infraestrutura Enterprise, Helm Charts, Load Testing & Documentação OpenAPI
*Objetivo: Garantir deploy simples, alta escalabilidade e conformidade para uso em produção.*

#### [NEW] [infra/helm/api-monitoring-platform](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/infra/helm)
- Helm Chart completo parametrizado para deploy dos 5 microsserviços + PostgreSQL + Redis + Prometheus + Grafana em clusters Kubernetes (EKS, GKE, AKS, OpenShift).

#### [NEW] [infra/load-testing](file:///Users/rafael/Desktop/Plataforma-de-Monitoramento-de-APIs/infra/load-testing)
- Scripts k6 e Locust para simulação de carga (1.000 a 50.000 requisições/segundo) testando a capacidade do pipeline e identificando gargalos.

#### [MODIFY] Todos os Microsserviços Java
- Adicionar dependência `springdoc-openapi-starter-webmvc-ui` e anotações `@Operation`, `@ApiResponse` para documentação Swagger/OpenAPI 3.0 unificada.
- Adicionar migrações de banco com **Flyway** em `storage-layer/src/main/resources/db/migration/`.

---

## Plan de Verificação

### Testes Automatizados
- Executar suíte completa de testes unitários e de integração:
  ```bash
  mvn clean test
  ```
- Executar testes de integração com banco real e containers via Testcontainers:
  ```bash
  cd storage-layer && mvn test
  ```
- Verificar cobertura de código JaCoCo (meta ≥ 80%):
  ```bash
  mvn test jacoco:report
  ```

### Testes Manuais & Validação Visual
- Iniciar ambiente local via Docker Compose:
  ```bash
  docker compose up -d
  ```
- Verificar saúde dos Actuator Health Checks em todas as portas (`8081`, `8082`, `8083`, `8084`, `8085`).
- Acessar o novo Frontend Web Dashboard no navegador e validar atualização dinâmica de métricas, alertas e chamadas de API.
