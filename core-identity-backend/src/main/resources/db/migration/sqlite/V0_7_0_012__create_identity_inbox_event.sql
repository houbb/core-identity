-- V0.7.0.012: Create identity_inbox_event table (P7.3)
CREATE TABLE IF NOT EXISTS identity_inbox_event (
    id            VARCHAR(36)  NOT NULL PRIMARY KEY,
    consumer_name VARCHAR(150) NOT NULL,
    event_id      VARCHAR(36)  NOT NULL,
    event_type    VARCHAR(150) NOT NULL,
    status        VARCHAR(30)  NOT NULL DEFAULT 'PROCESSED',
    payload_hash  VARCHAR(128) NULL,
    received_at   BIGINT       NOT NULL,
    processed_at  BIGINT       NULL,
    error_code    VARCHAR(100) NULL,
    retry_count   INTEGER      NOT NULL DEFAULT 0,
    version       BIGINT       NOT NULL DEFAULT 1
);

CREATE UNIQUE INDEX uq_inbox_consumer_event ON identity_inbox_event(consumer_name, event_id);