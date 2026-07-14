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
    occurred_at    BIGINT       NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_identity_audit_event_occurred_at
    ON identity_audit_event(occurred_at);
CREATE INDEX IF NOT EXISTS idx_identity_audit_event_event_type
    ON identity_audit_event(event_type);
CREATE INDEX IF NOT EXISTS idx_identity_audit_event_actor
    ON identity_audit_event(actor_id, actor_type);
CREATE INDEX IF NOT EXISTS idx_identity_audit_event_request_id
    ON identity_audit_event(request_id);
CREATE INDEX IF NOT EXISTS idx_identity_audit_event_target
    ON identity_audit_event(target_type, target_id);