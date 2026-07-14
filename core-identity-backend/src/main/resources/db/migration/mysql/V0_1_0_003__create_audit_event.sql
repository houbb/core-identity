-- V0.1.0.003: Create identity_audit_event table
CREATE TABLE IF NOT EXISTS identity_audit_event (
    id             VARCHAR(36)  NOT NULL PRIMARY KEY,
    event_type     VARCHAR(100) NOT NULL,
    actor_type     VARCHAR(30)  NOT NULL,
    actor_id       VARCHAR(100) NOT NULL,
    action         VARCHAR(100) NOT NULL,
    target_type    VARCHAR(100),
    target_id      VARCHAR(100),
    result         VARCHAR(20)  NOT NULL DEFAULT 'SUCCESS',
    reason         VARCHAR(500),
    request_id     VARCHAR(64),
    source_service VARCHAR(100),
    source_ip      VARCHAR(64),
    user_agent     VARCHAR(500),
    metadata_json  TEXT,
    occurred_at    BIGINT       NOT NULL,

    INDEX idx_identity_audit_event_occurred_at (occurred_at),
    INDEX idx_identity_audit_event_event_type (event_type),
    INDEX idx_identity_audit_event_actor (actor_id, actor_type),
    INDEX idx_identity_audit_event_request_id (request_id),
    INDEX idx_identity_audit_event_target (target_type, target_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;