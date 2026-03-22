CREATE TABLE IF NOT EXISTS agent_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    description VARCHAR(1024),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

ALTER TABLE agents
    ADD COLUMN IF NOT EXISTS display_name VARCHAR(255);

ALTER TABLE agents
    ADD COLUMN IF NOT EXISTS host_ip VARCHAR(255);

ALTER TABLE agents
    ADD COLUMN IF NOT EXISTS group_id BIGINT;

UPDATE agents
SET display_name = agent_id
WHERE display_name IS NULL;

DO
$$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_agents_group'
    ) THEN
        ALTER TABLE agents
            ADD CONSTRAINT fk_agents_group
                FOREIGN KEY (group_id) REFERENCES agent_groups (id);
    END IF;
END
$$;

CREATE INDEX IF NOT EXISTS idx_agents_group_id ON agents(group_id);
