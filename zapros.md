# Frontend API

Base URL:

```text
http://194.113.106.38:8087/api/v1
```

Swagger UI:

```text
http://194.113.106.38:8087/docs
```

OpenAPI JSON:

```text
http://194.113.106.38:8087/docs/openapi
```

WebSocket:

```text
ws://194.113.106.38:8087/api/v1/live/ws
```

SSE:

```text
http://194.113.106.38:8087/api/v1/live/sse
```

## Health

```http
GET /api/v1/health
```

Response:

```json
{
  "status": "UP",
  "dependencies": {
    "opensearch": "UP",
    "postgres": "UP"
  }
}
```

If OpenSearch is disabled in config, response becomes:

```json
{
  "status": "UP",
  "dependencies": {
    "opensearch": "DISABLED",
    "postgres": "UP"
  }
}
```

## Logs Search

```http
GET /api/v1/logs/search?q=database&host=srv-1&service=backend&level=ERROR&from=2026-03-21T11:00:00Z&to=2026-03-21T12:00:00Z&page=0&size=20
```

Query params:

- `q`
- `host`
- `service`
- `level`
- `from`
- `to`
- `page`
- `size`

Response:

```json
{
  "items": [
    {
      "timestamp": "2026-03-21T11:33:50Z",
      "level": "ERROR",
      "message": "database failed 2",
      "host": "srv-1",
      "service": "backend",
      "sourceType": "file",
      "sourcePath": "/var/log/app.log",
      "tags": ["prod"],
      "metadata": {
        "env": "prod"
      },
      "agentId": "agent-1"
    }
  ],
  "total": 1,
  "aggregations": {
    "hosts": {
      "srv-1": 1
    },
    "services": {
      "backend": 1
    },
    "levels": {
      "ERROR": 1
    }
  }
}
```

## Dashboard

Summary:

```http
GET /api/v1/dashboard/summary?from=2026-03-21T11:00:00Z&to=2026-03-21T12:00:00Z
```

```json
{
  "totalLogs": 120,
  "errors": 18,
  "warnings": 22,
  "hosts": 4,
  "services": 6
}
```

Timeseries:

```http
GET /api/v1/dashboard/timeseries?from=2026-03-21T11:00:00Z&to=2026-03-21T12:00:00Z
```

```json
{
  "buckets": [
    {
      "timestamp": "2026-03-21T11:00:00Z",
      "errors": 3,
      "warnings": 2
    }
  ]
}
```

Top hosts:

```http
GET /api/v1/dashboard/top-hosts?from=2026-03-21T11:00:00Z&to=2026-03-21T12:00:00Z&limit=10
```

```json
{
  "items": [
    {
      "host": "srv-1",
      "count": 53
    },
    {
      "host": "srv-2",
      "count": 17
    }
  ]
}
```

Top services:

```http
GET /api/v1/dashboard/top-services?from=2026-03-21T11:00:00Z&to=2026-03-21T12:00:00Z&limit=10
```

```json
{
  "items": [
    {
      "service": "backend",
      "count": 53
    },
    {
      "service": "postgres",
      "count": 11
    }
  ]
}
```

## Agents

List:

```http
GET /api/v1/agents
```

```json
[
  {
    "agentId": "agent-1",
    "host": "srv-1",
    "status": "ONLINE",
    "lastSeen": "2026-03-21T11:24:32.636001841Z",
    "bufferedCount": 0
  }
]
```

Details:

```http
GET /api/v1/agents/agent-1
```

Heartbeat:

```http
POST /api/v1/agents/heartbeat
Content-Type: application/json
```

```json
{
  "agentId": "agent-1",
  "host": "srv-1",
  "status": "ONLINE",
  "bufferedCount": 0
}
```

## Ingest

```http
POST /api/v1/ingest/batch
Content-Type: application/json
```

```json
{
  "agentId": "agent-1",
  "events": [
    {
      "timestamp": "2026-03-21T11:40:00Z",
      "level": "ERROR",
      "message": "database timeout",
      "host": "srv-1",
      "service": "backend",
      "sourceType": "file",
      "sourcePath": "/var/log/app.log",
      "tags": ["prod", "db"],
      "metadata": {
        "env": "prod",
        "component": "jdbc"
      }
    }
  ]
}
```

```json
{
  "status": "ok",
  "accepted": 1,
  "requestId": "27aa717b-688f-458c-954f-f8fa8d8cbd01"
}
```

## Alerts

List rules:

```http
GET /api/v1/alerts/rules
```

Create:

```http
POST /api/v1/alerts/rules
Content-Type: application/json
```

```json
{
  "name": "DB errors to Telegram",
  "enabled": true,
  "conditionType": "count_gt",
  "query": "database",
  "threshold": 1,
  "windowSeconds": 300,
  "cooldownSeconds": 300,
  "channel": "telegram",
  "level": "ERROR"
}
```

Supported values:

- `conditionType`: `count_gt`, `contains_pattern`, `error_rate_gt`
- `channel`: `telegram`, `webhook`

Rule response:

```json
{
  "id": 1,
  "name": "DB errors to Telegram",
  "enabled": true,
  "conditionType": "count_gt",
  "query": "database",
  "threshold": 1,
  "windowSeconds": 300,
  "cooldownSeconds": 300,
  "channel": "telegram",
  "level": "ERROR",
  "createdAt": "2026-03-21T11:27:14.296289Z",
  "updatedAt": "2026-03-21T11:27:14.296289Z",
  "lastTriggeredAt": null
}
```

Update:

```http
PUT /api/v1/alerts/rules/1
```

Delete:

```http
DELETE /api/v1/alerts/rules/1
```

History:

```http
GET /api/v1/alerts/history
```

```json
[
  {
    "id": 1,
    "ruleId": 1,
    "ruleName": "DB errors to Telegram",
    "triggeredAt": "2026-03-21T11:36:55.691Z",
    "message": "Alert 'DB errors to Telegram' triggered: value=2 threshold=1 window=300s",
    "channel": "telegram",
    "value": 2,
    "threshold": 1
  }
]
```

## Realtime

WebSocket:

```text
ws://194.113.106.38:8087/api/v1/live/ws
```

Message example:

```json
{
  "type": "log_event",
  "payload": {
    "timestamp": "2026-03-21T11:40:00Z",
    "level": "ERROR",
    "message": "database timeout",
    "host": "srv-1",
    "service": "backend",
    "sourceType": "file",
    "sourcePath": "/var/log/app.log",
    "tags": ["prod", "db"],
    "metadata": {
      "env": "prod"
    },
    "agentId": "agent-1"
  }
}
```

Event types:

- `log_event`
- `agent_status`
- `alert_triggered`

SSE:

```http
GET /api/v1/live/sse
```
