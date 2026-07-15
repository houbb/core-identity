-- V0.4.0.002: Create identity_scope_permission table
CREATE TABLE IF NOT EXISTS identity_scope_permission (
    id            VARCHAR(36) NOT NULL PRIMARY KEY,
    scope_id      VARCHAR(36) NOT NULL,
    permission_id VARCHAR(36) NOT NULL,
    created_at    BIGINT      NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_scope_permission
    ON identity_scope_permission(scope_id, permission_id);
CREATE INDEX IF NOT EXISTS idx_identity_scope_permission_scope
    ON identity_scope_permission(scope_id);
CREATE INDEX IF NOT EXISTS idx_identity_scope_permission_perm
    ON identity_scope_permission(permission_id);