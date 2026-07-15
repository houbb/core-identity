-- V0.4.0.033: Create identity_authorization_grant_scope table (SQLite)
CREATE TABLE IF NOT EXISTS identity_authorization_grant_scope (
    id         TEXT    NOT NULL PRIMARY KEY,
    grant_id   TEXT    NOT NULL,
    scope_id   TEXT    NOT NULL,
    created_at INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_identity_ags_grant ON identity_authorization_grant_scope(grant_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_ags_grant_scope ON identity_authorization_grant_scope(grant_id, scope_id);
