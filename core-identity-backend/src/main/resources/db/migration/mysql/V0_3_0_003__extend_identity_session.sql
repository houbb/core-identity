-- V0.3.0.003: Extend identity_session for P2 (MySQL)
ALTER TABLE identity_session
    ADD COLUMN last_organization_id VARCHAR(36) NULL,
    ADD COLUMN permission_version BIGINT NOT NULL DEFAULT 0;

CREATE INDEX idx_identity_session_last_org ON identity_session(last_organization_id);
