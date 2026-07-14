-- V0.1.0.005: Create identity_idempotency_record table
CREATE TABLE IF NOT EXISTS identity_idempotency_record (
    id              VARCHAR(36)  NOT NULL PRIMARY KEY,
    idempotency_key VARCHAR(150) NOT NULL,
    scope           VARCHAR(100) NOT NULL,
    request_hash    VARCHAR(128),
    status          VARCHAR(20)  NOT NULL DEFAULT 'PROCESSING',
    response_status INTEGER,
    response_body   TEXT,
    locked_until    BIGINT,
    expires_at      BIGINT       NOT NULL,
    created_at      BIGINT       NOT NULL,
    updated_at      BIGINT       NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_identity_idempotency_scope_key
    ON identity_idempotency_record(scope, idempotency_key);
CREATE INDEX IF NOT EXISTS idx_identity_idempotency_expires_at
    ON identity_idempotency_record(expires_at);
CREATE INDEX IF NOT EXISTS idx_identity_idempotency_status
    ON identity_idempotency_record(status);