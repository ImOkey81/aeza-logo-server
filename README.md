# Logovizor Spring Server (MVP)

Spring Boot backend for Logovizor MVP:
- ingest logs from agents
- full-text search
- dashboard aggregations
- live stream (SSE)
- alert rules + scheduler
- agents status
- simple admin auth

## Run

1. Create PostgreSQL database `logovizor`.
2. Copy env vars from `.env.example`.
3. Start server:

```bash
./gradlew bootRun
```

## Database

The server uses PostgreSQL via Spring Data JPA.
Schema is created/updated automatically by Hibernate (`ddl-auto=update`).
Reference SQL migration is in `src/main/resources/db/migration`.

## Key endpoints

- `POST /api/v1/auth/login`
- `GET /api/v1/me`
- `POST /api/v1/ingest/batch` (`X-Agent-Token`)
- `POST /api/v1/agents/heartbeat` (`X-Agent-Token`)
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
- `GET /api/v1/live/ws` (SSE stream)
- `GET /api/v1/health`


# aeza-logo-server
