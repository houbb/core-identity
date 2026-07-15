-- V0.2.0.009: Create identity_platform_operator table
CREATE TABLE IF NOT EXISTS identity_platform_operator (
    id            VARCHAR(36) NOT NULL PRIMARY KEY,
    user_id       VARCHAR(36) NOT NULL,
    operator_role VARCHAR(40) NOT NULL DEFAULT 'SUPER_ADMIN',
    status        VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    granted_by    VARCHAR(36),
    granted_at    BIGINT      NOT NULL,
    disabled_at   BIGINT,
    created_at    BIGINT      NOT NULL,
    updated_at    BIGINT      NOT NULL,
    version       BIGINT      NOT NULL DEFAULT 1
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_platform_operator_user
    ON identity_platform_operator(user_id);
CREATE INDEX IF NOT EXISTS idx_identity_platform_operator_role
    ON identity_platform_operator(operator_role);