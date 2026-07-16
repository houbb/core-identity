CREATE TABLE IF NOT EXISTS identity_access_grant (
    id              TEXT    NOT NULL,
    subject_type    TEXT    NOT NULL,
    subject_id      TEXT    NOT NULL,
    organization_id TEXT,
    entitlement_id  TEXT    NOT NULL,
    source_type     TEXT    NOT NULL,
    source_id       TEXT,
    grant_type      TEXT    NOT NULL DEFAULT 'STANDARD',
    status          TEXT    NOT NULL DEFAULT 'ACTIVE',
    valid_from      INTEGER NOT NULL,
    expires_at      INTEGER,
    granted_by      TEXT,
    revoked_by      TEXT,
    revoked_at      INTEGER,
    revoke_reason   TEXT,
    last_used_at    INTEGER,
    created_at      INTEGER NOT NULL,
    updated_at      INTEGER NOT NULL,
    version         INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_grant_subject ON identity_access_grant(subject_type, subject_id);
CREATE INDEX idx_identity_grant_subject_org ON identity_access_grant(subject_id, organization_id);
CREATE INDEX idx_identity_grant_entitlement ON identity_access_grant(entitlement_id);
CREATE INDEX idx_identity_grant_status ON identity_access_grant(status);
CREATE INDEX idx_identity_grant_expires ON identity_access_grant(expires_at);
CREATE INDEX idx_identity_grant_source ON identity_access_grant(source_type, source_id);
