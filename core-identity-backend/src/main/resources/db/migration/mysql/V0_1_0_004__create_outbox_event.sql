-- V0.1.0.004: Create identity_outbox_event table
CREATE TABLE IF NOT EXISTS identity_outbox_event (
    id              VARCHAR(36)  NOT NULL PRIMARY KEY,
    event_type      VARCHAR(120) NOT NULL,
    event_version   INTEGER      NOT NULL DEFAULT 1,
    aggregate_type  VARCHAR(100) NOT NULL,
    aggregate_id    VARCHAR(100) NOT NULL,
    payload_json    TEXT         NOT NULL,
    headers_json    TEXT,
    status          VARCHAR(20)  NOT NULL DEFAULT 'PENDING',
    attempt_count   INTEGER      NOT NULL DEFAULT 0,
    next_attempt_at BIGINT,
    published_at    BIGINT,
    last_error      TEXT,
    created_at      BIGINT       NOT NULL,
    updated_at      BIGINT       NOT NULL,
    version         BIGINT       NOT NULL DEFAULT 1,

    INDEX idx_identity_outbox_status_next_attempt (status, next_attempt_at),
    INDEX idx_identity_outbox_created_at (created_at),
    INDEX idx_identity_outbox_event_type (event_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;