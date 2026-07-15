-- V0.4.0.033: Create identity_authorization_grant_scope table
CREATE TABLE IF NOT EXISTS identity_authorization_grant_scope (
    id         VARCHAR(36) NOT NULL PRIMARY KEY,
    grant_id   VARCHAR(36) NOT NULL,
    scope_id   VARCHAR(36) NOT NULL,
    created_at BIGINT      NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX IF NOT EXISTS idx_identity_ags_grant ON identity_authorization_grant_scope(grant_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_ags_grant_scope ON identity_authorization_grant_scope(grant_id, scope_id);
