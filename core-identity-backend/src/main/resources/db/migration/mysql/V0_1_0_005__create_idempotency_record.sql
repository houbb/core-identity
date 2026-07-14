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
    updated_at      BIGINT       NOT NULL,

    UNIQUE INDEX idx_identity_idempotency_scope_key (scope, idempotency_key),
    INDEX idx_identity_idempotency_expires_at (expires_at),
    INDEX idx_identity_idempotency_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;