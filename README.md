# Logovizor Server

Spring Boot backend for centralized log ingestion, search and analytics.

What the server provides:
- receives batched log events from agents
- receives heartbeat/status updates from agents
- stores events in PostgreSQL
- indexes/searches events through OpenSearch
- exposes dashboard, search and agent status APIs
- streams live updates through SSE and WebSocket
- supports alert rules with Telegram/Webhook channels
- exposes OpenAPI/Swagger docs

## Stack

- Java 21
- Spring Boot
- PostgreSQL 16
- OpenSearch 2
- Docker / Docker Compose

## Configuration

The repository keeps `src/main/resources/application.yml` as a safe local example without secrets.
Real local overrides should be kept in ignored files such as:

- `src/main/resources/application.local.yml`
- `src/main/resources/application.override.yml`
- `src/main/resources/application.secrets.yml`

Example `application.yml`:

```yaml
spring:
  application:
    name: aeza-server
  datasource:
    url: jdbc:postgresql://localhost:5432/logovizor
    username: postgres
    password: ""
  jpa:
    hibernate:
      ddl-auto: update
    open-in-view: false

server:
  address: 0.0.0.0
  port: 8087

cors:
  allowed-origin-patterns:
    - "*"

opensearch:
  enabled: true
  url: http://localhost:9200
  username: ""
  password: ""
  index-prefix: log-events

alert:
  scheduler:
    interval-ms: 30000

integration:
  telegram:
    bot-token: ""
    chat-id: ""
  webhook:
    url: ""

springdoc:
  swagger-ui:
    path: /docs
    url: /openapi.yaml
    disable-swagger-default-url: true
  api-docs:
    path: /docs/openapi
```

## Run Locally

1. Start infrastructure:

```bash
docker compose up -d postgres opensearch
```

2. Run the backend:

```bash
./gradlew bootRun
```

3. Check health:

```bash
curl http://127.0.0.1:8087/api/v1/health
```

## Run With Docker Compose

Run the full backend stack:

```bash
docker compose up -d --build
```

Check:

```bash
docker compose ps
curl http://127.0.0.1:8087/api/v1/health
```

Stop:

```bash
docker compose down
```

Stop and remove volumes:

```bash
docker compose down -v
```

## Kubernetes

The repository now includes Kubernetes manifests in `k8s/`.

Apply everything:

```bash
kubectl apply -k k8s
```

Check status:

```bash
kubectl get all -n logovizor
kubectl get pvc -n logovizor
```

Open the backend locally:

```bash
kubectl port-forward svc/aeza-backend 8087:8087 -n logovizor
curl http://127.0.0.1:8087/api/v1/health
```

Before applying:

- update `k8s/secret.yaml` with real passwords and tokens
- update the backend image in `k8s/backend.yaml` to the image published by your CI/CD
- ensure your cluster has a default `StorageClass` for PVC provisioning

## GitHub Actions CI/CD

The workflow is stored at `.github/workflows/ci-cd.yml`.

Pipeline behavior:

- runs tests with Gradle
- builds the project
- builds a Docker image
- pushes the image to the configured registry on `main` or `master`

Required GitHub configuration:

- repository secret `REGISTRY_USERNAME`
- repository secret `REGISTRY_PASSWORD`
- repository variable `REGISTRY` such as `ghcr.io`
- repository variable `IMAGE_NAME` such as `your-user-or-org/aeza-logo-server`

## API

Main endpoints:

- `POST /api/v1/ingest/batch`
- `POST /api/v1/agents/heartbeat`
- `GET /api/v1/logs/search`
- `GET /api/v1/dashboard/summary`
- `GET /api/v1/dashboard/timeseries`
- `GET /api/v1/dashboard/top-hosts`
- `GET /api/v1/dashboard/top-services`
- `GET /api/v1/dashboard/top-patterns`
- `GET /api/v1/agents`
- `GET /api/v1/alerts/rules`
- `POST /api/v1/alerts/rules`
- `PUT /api/v1/alerts/rules/{id}`
- `DELETE /api/v1/alerts/rules/{id}`
- `GET /api/v1/alerts/history`
- `GET /api/v1/live/sse`
- `WS /api/v1/live/ws`
- `GET /api/v1/health`

## Swagger / OpenAPI

- Swagger UI: `http://127.0.0.1:8087/docs`
- OpenAPI JSON: `http://127.0.0.1:8087/docs/openapi`
- OpenAPI YAML: `http://127.0.0.1:8087/openapi.yaml`

## Quick Smoke Check

Health:

```bash
curl http://127.0.0.1:8087/api/v1/health
```

Manual heartbeat:

```bash
curl -X POST http://127.0.0.1:8087/api/v1/agents/heartbeat \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "agent-dev-01",
    "hostName": "agent-vm-01",
    "hostIp": "158.160.205.79",
    "status": "online",
    "bufferedCount": 0,
    "lastSequence": 1774122410076,
    "watchedSources": ["/var/log/nginx/error.log", "/var/log/syslog"]
  }'
```

Manual ingest:

```bash
curl -X POST http://127.0.0.1:8087/api/v1/ingest/batch \
  -H "Content-Type: application/json" \
  -d '{
    "agentId": "agent-dev-01",
    "sequence": 1774122410077,
    "events": [
      {
        "timestamp": "2026-03-22T12:00:00Z",
        "level": "ERROR",
        "message": "database failed 42",
        "host": "agent-vm-01",
        "service": "nginx",
        "sourceType": "file",
        "sourcePath": "/var/log/nginx/error.log",
        "tags": ["prod", "agent"],
        "metadata": {
          "fingerprint": "d5c98f0a8be1d4ac",
          "messageTemplate": "database failed <num>",
          "occurrences": 12,
          "aggregated": true,
          "sampled": true,
          "burstDetected": true
        }
      }
    ]
  }'
```
