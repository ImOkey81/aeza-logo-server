CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(128) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(32) NOT NULL
);

CREATE TABLE IF NOT EXISTS agents (
    agent_id VARCHAR(128) PRIMARY KEY,
    host VARCHAR(255) NOT NULL,
    status VARCHAR(32) NOT NULL,
    last_seen TIMESTAMP WITH TIME ZONE NOT NULL,
    buffered_count INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS alert_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL,
    condition_type VARCHAR(64) NOT NULL,
    query_text TEXT,
    threshold BIGINT NOT NULL,
    window_seconds BIGINT NOT NULL,
    cooldown_seconds BIGINT NOT NULL,
    channel VARCHAR(32) NOT NULL,
    level VARCHAR(32),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    last_triggered_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS alert_events (
    id BIGSERIAL PRIMARY KEY,
    rule_id BIGINT NOT NULL,
    rule_name VARCHAR(255) NOT NULL,
    triggered_at TIMESTAMP WITH TIME ZONE NOT NULL,
    message TEXT NOT NULL,
    channel VARCHAR(32) NOT NULL,
    value BIGINT NOT NULL,
    threshold BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS log_events (
    id BIGSERIAL PRIMARY KEY,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL,
    level VARCHAR(32) NOT NULL,
    message TEXT NOT NULL,
    host VARCHAR(255) NOT NULL,
    service VARCHAR(255) NOT NULL,
    source_type VARCHAR(128),
    source_path VARCHAR(1024),
    tags TEXT,
    metadata TEXT,
    agent_id VARCHAR(128) NOT NULL
);
