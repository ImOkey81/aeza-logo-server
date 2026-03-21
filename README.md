# Logovizor Spring Server (MVP)

Spring Boot backend for Logovizor MVP:
- ingest logs from agents
- full-text search
- dashboard aggregations
- live stream (WebSocket + SSE)
- alert rules + scheduler
- agents status
- no authentication required

## Run With Docker Compose (Recommended)

This repository includes a full stack setup for server deployment:
- backend (Spring Boot)
- PostgreSQL
- OpenSearch

Steps:

```bash
cp .env.example .env
docker compose up -d --build
```

Check services:

```bash
docker compose ps
curl -s http://localhost:8087/api/v1/health
```

Stop:

```bash
docker compose down
```

Stop and delete data volumes:

```bash
docker compose down -v
```

## Database

The server uses PostgreSQL via Spring Data JPA and can also index/search in OpenSearch.
Ingest stores events in PostgreSQL and indexes them in OpenSearch when enabled.
Schema is created/updated automatically by Hibernate (`ddl-auto=update`).
Reference SQL migration is in `src/main/resources/db/migration`.

## Key endpoints

- `POST /api/v1/ingest/batch`
- `POST /api/v1/agents/heartbeat`
- `GET /api/v1/logs/search`
- `GET /api/v1/dashboard/summary`
- `GET /api/v1/dashboard/timeseries`
- `GET /api/v1/dashboard/top-hosts`
- `GET /api/v1/dashboard/top-services`
- `GET /api/v1/agents`
- `GET /api/v1/alerts/rules`
- `POST /api/v1/alerts/rules`
- `PUT /api/v1/alerts/rules/{id}`
- `DELETE /api/v1/alerts/rules/{id}`
- `GET /api/v1/alerts/history`
- `WS /api/v1/live/ws` (WebSocket stream)
- `GET /api/v1/live/sse` (SSE stream)
- `GET /api/v1/health`

## WebSocket Quick Check

1. Connect client to `ws://<SERVER_IP>:8087/api/v1/live/ws`.
2. Send ingest event:

```bash
curl -s -X POST "http://<SERVER_IP>:8087/api/v1/ingest/batch" \
  -H "Content-Type: application/json" \
  -d '{"agentId":"agent-check","events":[{"timestamp":"2026-03-21T12:50:00Z","level":"ERROR","message":"ws-check","host":"host-check","service":"api","sourceType":"file","sourcePath":"/var/log/app.log","tags":["test"],"metadata":{"env":"prod"}}]}'
```

3. WebSocket client should receive `log_event`.


# aeza-logo-server
