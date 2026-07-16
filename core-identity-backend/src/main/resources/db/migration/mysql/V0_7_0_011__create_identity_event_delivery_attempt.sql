-- V0.7.0.011: Create identity_event_delivery_attempt table (P7.3)
CREATE TABLE IF NOT EXISTS identity_event_delivery_attempt (
    id              VARCHAR(36)  NOT NULL PRIMARY KEY,
    outbox_event_id VARCHAR(36)  NOT NULL,
    destination     VARCHAR(200) NOT NULL,
    attempt_number  INTEGER      NOT NULL DEFAULT 1,
    status          VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    response_code   INTEGER      NULL,
    error_code      VARCHAR(100) NULL,
    started_at      BIGINT       NOT NULL,
    completed_at    BIGINT       NULL,
    next_attempt_at BIGINT       NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_event_delivery_outbox ON identity_event_delivery_attempt(outbox_event_id);
CREATE INDEX idx_event_delivery_status ON identity_event_delivery_attempt(status);