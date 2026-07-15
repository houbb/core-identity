-- V0.3.0.021: Create identity_role_permission table
CREATE TABLE IF NOT EXISTS identity_role_permission (
    role_id       VARCHAR(36) NOT NULL,
    permission_id VARCHAR(36) NOT NULL,
    granted_by    VARCHAR(36),
    created_at    BIGINT      NOT NULL,
    PRIMARY KEY (role_id, permission_id)
);

CREATE INDEX IF NOT EXISTS idx_identity_role_permission_permission
    ON identity_role_permission(permission_id);
