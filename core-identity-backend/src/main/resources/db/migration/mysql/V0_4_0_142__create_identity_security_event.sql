-- V0.4.0.142: Create identity_security_event table
CREATE TABLE IF NOT EXISTS identity_security_event (
    id                 VARCHAR(36)   NOT NULL,
    user_id            VARCHAR(36),
    organization_id    VARCHAR(36),
    event_type         VARCHAR(100)  NOT NULL,
    severity           VARCHAR(20)   NOT NULL DEFAULT 'INFO',
    status             VARCHAR(30)   NOT NULL DEFAULT 'OPEN',
    source             VARCHAR(50),
    risk_assessment_id VARCHAR(36),
    title              VARCHAR(200),
    description        VARCHAR(1000),
    metadata_json      TEXT,
    detected_at        BIGINT        NOT NULL,
    resolved_at        BIGINT,
    resolved_by        VARCHAR(36),
    resolution         VARCHAR(500),
    created_at         BIGINT        NOT NULL,
    updated_at         BIGINT        NOT NULL,
    version            BIGINT        NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_security_event_user ON identity_security_event(user_id, status);
CREATE INDEX idx_identity_security_event_org ON identity_security_event(organization_id);
CREATE INDEX idx_identity_security_event_type ON identity_security_event(event_type);
CREATE INDEX idx_identity_security_event_detected ON identity_security_event(detected_at);