-- V0.1.0.002: Create identity_internal_client table
CREATE TABLE IF NOT EXISTS identity_internal_client (
    id                 VARCHAR(36)  NOT NULL PRIMARY KEY,
    client_id          VARCHAR(100) NOT NULL,
    client_secret_hash VARCHAR(255) NOT NULL,
    display_name       VARCHAR(150) NOT NULL,
    client_type        VARCHAR(30)  NOT NULL DEFAULT 'SERVICE',
    scopes_json        TEXT,
    status             VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    expires_at         BIGINT,
    last_used_at       BIGINT,
    created_at         BIGINT       NOT NULL,
    updated_at         BIGINT       NOT NULL,
    version            BIGINT       NOT NULL DEFAULT 1
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_identity_internal_client_client_id
    ON identity_internal_client(client_id);
CREATE INDEX IF NOT EXISTS idx_identity_internal_client_status
    ON identity_internal_client(status);
CREATE INDEX IF NOT EXISTS idx_identity_internal_client_expires_at
    ON identity_internal_client(expires_at);