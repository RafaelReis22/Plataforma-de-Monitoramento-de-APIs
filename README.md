# API Monitoring Platform

Plataforma completa de monitoramento de APIs com métricas de hardware, análise HTTP, pipeline de observabilidade e dashboards Grafana. Desenvolvida com Java 21 e Spring Boot 3.3.4.

## Arquitetura

```
┌─────────────────────────────────────────────────────────────────┐
│                    API Monitoring Platform                       │
│                                                                  │
│  ┌─────────────────┐    ┌──────────────────┐                   │
│  │ monitoring-agent│    │ api-interceptor  │                   │
│  │    :8081        │    │     :8082        │                   │
│  │  CPU/MEM/DISK   │    │  HTTP Filter +   │                   │
│  │  OSHI + Spring  │    │  Redis Cache     │                   │
│  └────────┬────────┘    └────────┬─────────┘                   │
│           │                     │                               │
│           └──────────┬──────────┘                               │
│                      ▼                                           │
│         ┌────────────────────────┐                              │
│         │  observability-pipeline│                              │
│         │        :8083           │                              │
│         │  Agregação + Redis     │                              │
│         └───────────┬────────────┘                              │
│                     │                                            │
│           ┌─────────┴──────────┐                                │
│           ▼                    ▼                                 │
│  ┌─────────────────┐  ┌─────────────────┐                      │
│  │  storage-layer  │  │dashboard-service│                      │
│  │     :8084       │  │     :8085       │                      │
│  │  PostgreSQL +   │  │  PromQL Client  │                      │
│  │  PromQL Client  │  │  REST API       │                      │
│  └─────────────────┘  └─────────────────┘                      │
│                                                                  │
│  ┌──────────────┐  ┌──────────┐  ┌────────────┐               │
│  │  Prometheus  │  │ Grafana  │  │ PostgreSQL │               │
│  │    :9090     │  │  :3000   │  │   :5432    │               │
│  └──────────────┘  └──────────┘  └────────────┘               │
└─────────────────────────────────────────────────────────────────┘
```

## Pré-requisitos

- **Java 21** (OpenJDK ou Temurin)
- **Maven 3.9+**
- **Docker 24+** e **Docker Compose v2**
- **Git**

Verificar instalações:

```bash
java -version     # deve mostrar 21.x
mvn -version      # deve mostrar 3.9.x
docker -v         # deve mostrar 24.x
docker compose version  # deve mostrar v2.x
```

## Início Rápido (5 minutos)

### 1. Clonar o repositório

```bash
git clone https://github.com/RafaelReis22/api-monitoring-platform.git
cd api-monitoring-platform
```

### 2. Subir infraestrutura

```bash
docker compose up -d
```

Aguardar todos os serviços ficarem saudáveis:

```bash
docker compose ps
```

### 3. Compilar e executar os serviços Java

```bash
# Compilar todos os módulos
mvn clean package -DskipTests

# Executar cada serviço em terminal separado (ou em background)
cd monitoring-agent    && java -jar target/*.jar &
cd api-gateway-interceptor && java -jar target/*.jar &
cd observability-pipeline  && java -jar target/*.jar &
cd storage-layer           && java -jar target/*.jar &
cd dashboard-service       && java -jar target/*.jar &
```

### 4. Verificar saúde dos serviços

```bash
curl http://localhost:8081/actuator/health
curl http://localhost:8082/actuator/health
curl http://localhost:8083/actuator/health
curl http://localhost:8084/actuator/health
curl http://localhost:8085/actuator/health
```

Todos devem retornar `{"status":"UP"}`.

### 5. Acessar dashboards

| URL | Serviço | Credenciais |
|-----|---------|-------------|
| http://localhost:3000 | Grafana | admin / admin |
| http://localhost:9090 | Prometheus | — |
| http://localhost:8081/actuator/prometheus | Métricas agent | — |

## Executar Testes

```bash
# Todos os testes (unitários + integração)
mvn test

# Apenas storage-layer com Testcontainers
cd storage-layer && mvn test

# Com relatório de cobertura
mvn test jacoco:report
# relatório: target/site/jacoco/index.html
```

> **Atenção:** os testes de integração (`MetricHistoryE2ETest`) usam Testcontainers e requerem Docker rodando.

## Estrutura do Projeto

```
api-monitoring-platform/
├── monitoring-agent/           # Coleta CPU, memória, disco via OSHI
├── api-gateway-interceptor/    # Filter HTTP + cache Redis
├── observability-pipeline/     # Agregação de métricas
├── storage-layer/              # Persistência PostgreSQL + PromQL
├── dashboard-service/          # API REST para dashboards
├── infra/
│   ├── docker-compose.yml      # PostgreSQL, Redis, Prometheus, Grafana
│   ├── prometheus/             # prometheus.yml + regras de alerta
│   ├── grafana/dashboards/     # 4 dashboards JSON prontos para importar
│   ├── kubernetes/             # Manifests K8s completos
│   ├── n8n/                    # Workflows de alertas e SLO
│   └── security/               # Supressões OWASP
├── .github/workflows/
│   └── security-scan.yml       # OWASP + Gitleaks + Trivy + CodeQL
└── docs/design/                # Protótipos HTML das telas
```

## Serviços e Portas

| Serviço | Porta | Descrição |
|---------|-------|-----------|
| monitoring-agent | 8081 | Coleta hardware (CPU, memória, disco) |
| api-interceptor | 8082 | Proxy HTTP com métricas e cache |
| observability-pipeline | 8083 | Pipeline de agregação |
| storage-layer | 8084 | Histórico de métricas no PostgreSQL |
| dashboard-service | 8085 | API REST para o frontend |
| Prometheus | 9090 | TSDB de métricas |
| Grafana | 3000 | Dashboards visuais |
| PostgreSQL | 5432 | Banco relacional |
| Redis | 6379 | Cache L2 |

## Dashboards Grafana

Os dashboards JSON estão em `infra/grafana/dashboards/`. São provisionados automaticamente via Docker Compose.

| Dashboard | Arquivo | Descrição |
|-----------|---------|-----------|
| Hardware Overview | `hardware-overview.json` | CPU, memória, disco, rede |
| API Health | `api-health.json` | Throughput, latência P50/P95/P99, erros |
| JVM Internals | `jvm-internals.json` | Heap, GC pause, threads, CPU JVM |
| SLO Operacional | `slo-operacional.json` | Disponibilidade 24h, error budget |

### Importar dashboards manualmente

1. Abrir Grafana em http://localhost:3000
2. Menu lateral → **Dashboards** → **Import**
3. Fazer upload do arquivo JSON desejado
4. Selecionar datasource **Prometheus**

## Alertas

Regras configuradas em `infra/prometheus/alerts.yml`:

| Alerta | Severidade | Condição |
|--------|------------|----------|
| HighCpuUsage | warning | CPU > 80% por 5m |
| HighErrorRate | critical | Taxa 5xx > 5% por 2m |
| HighP99Latency | critical | P99 > 300ms por 5m |
| ServiceDown | critical | `up == 0` por 1m |
| HighJvmHeapUsage | warning | Heap > 85% por 10m |
| HighMemoryUsage | warning | RAM > 85% por 5m |

## Kubernetes

Deploy no cluster local com Kind ou Minikube:

```bash
# Criar namespace
kubectl apply -f infra/kubernetes/namespace.yml

# ConfigMaps e Secrets
kubectl apply -f infra/kubernetes/configmap.yml

# Infraestrutura
kubectl apply -f infra/kubernetes/postgresql.yml
kubectl apply -f infra/kubernetes/redis.yml
kubectl apply -f infra/kubernetes/prometheus.yml
kubectl apply -f infra/kubernetes/grafana.yml

# Serviços Java
kubectl apply -f infra/kubernetes/services.yml

# Ingress
kubectl apply -f infra/kubernetes/ingress.yml
```

Adicionar ao `/etc/hosts`:

```
127.0.0.1  monitoring.local
```

Acessar: http://monitoring.local/grafana

## Segurança

O pipeline de CI/CD executa automaticamente:

- **OWASP Dependency Check** — vulnerabilidades CVE (falha em CVSS ≥ 7)
- **Gitleaks** — detecção de secrets no histórico Git
- **Trivy** — scan de imagens Docker (SARIF → GitHub Security)
- **CodeQL** — análise estática Java (security-and-quality)

Executar localmente:

```bash
# OWASP check
mvn org.owasp:dependency-check-maven:check

# Scan de secrets (requer Gitleaks instalado)
gitleaks detect --source .
```

## Variáveis de Ambiente

Criar `.env` na raiz (não commitar):

```env
# PostgreSQL
POSTGRES_DB=monitoring_meta
POSTGRES_USER=monitoring
POSTGRES_PASSWORD=monitoring_secret

# Redis
REDIS_PASSWORD=redis_secret

# Grafana
GF_SECURITY_ADMIN_PASSWORD=admin

# NVD (para OWASP check)
NVD_API_KEY=sua-chave-aqui
```

## Design das Telas

Os protótipos HTML das telas estão em `docs/design/`:

```bash
# Abrir no browser (Linux/WSL)
xdg-open docs/design/01-hardware-overview.html

# macOS
open docs/design/01-hardware-overview.html
```

| Arquivo | Tela |
|---------|------|
| `01-hardware-overview.html` | Dashboard Hardware |
| `02-api-health.html` | Saúde das APIs |
| `03-jvm-internals.html` | Internals da JVM |
| `04-alertas.html` | Central de Alertas |
| `05-design-system.html` | Design System |

## Stack Tecnológica

| Camada | Tecnologia |
|--------|------------|
| Linguagem | Java 21 |
| Framework | Spring Boot 3.3.4 |
| Métricas | Micrometer 1.13.6 + Prometheus |
| Hardware | OSHI 6.6.3 |
| Banco de dados | PostgreSQL 16 |
| Cache | Redis 7 |
| Observabilidade | Prometheus 2.54 + Grafana 11.2 |
| Automação | n8n (alertas + SLO) |
| Container | Docker Compose + Kubernetes |
| CI/CD | GitHub Actions |
| Testes | JUnit 5 + Mockito + Testcontainers |
| Cobertura | JaCoCo ≥ 80% |

## Licença

MIT License — consultar `LICENSE` para detalhes.
