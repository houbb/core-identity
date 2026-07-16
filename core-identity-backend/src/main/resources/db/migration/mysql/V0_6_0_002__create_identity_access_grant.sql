CREATE TABLE IF NOT EXISTS identity_access_grant (
    id              VARCHAR(36)  NOT NULL,
    subject_type    VARCHAR(30)  NOT NULL,
    subject_id      VARCHAR(36)  NOT NULL,
    organization_id VARCHAR(36),
    entitlement_id  VARCHAR(36)  NOT NULL,
    source_type     VARCHAR(30)  NOT NULL,
    source_id       VARCHAR(36),
    grant_type      VARCHAR(30)  NOT NULL DEFAULT 'STANDARD',
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    valid_from      BIGINT       NOT NULL,
    expires_at      BIGINT,
    granted_by      VARCHAR(36),
    revoked_by      VARCHAR(36),
    revoked_at      BIGINT,
    revoke_reason   VARCHAR(500),
    last_used_at    BIGINT,
    created_at      BIGINT       NOT NULL,
    updated_at      BIGINT       NOT NULL,
    version         BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_grant_subject ON identity_access_grant(subject_type, subject_id);
CREATE INDEX idx_identity_grant_subject_org ON identity_access_grant(subject_id, organization_id);
CREATE INDEX idx_identity_grant_entitlement ON identity_access_grant(entitlement_id);
CREATE INDEX idx_identity_grant_status ON identity_access_grant(status);
CREATE INDEX idx_identity_grant_expires ON identity_access_grant(expires_at);
CREATE INDEX idx_identity_grant_source ON identity_access_grant(source_type, source_id);