-- V0.4.0.031: Create identity_authorization_grant table
CREATE TABLE IF NOT EXISTS identity_authorization_grant (
    id               VARCHAR(36) NOT NULL PRIMARY KEY,
    client_id        VARCHAR(36) NOT NULL,
    user_id          VARCHAR(36) NOT NULL,
    organization_id  VARCHAR(36),
    status           VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    first_granted_at BIGINT      NOT NULL,
    last_used_at     BIGINT,
    revoked_at       BIGINT,
    created_at       BIGINT      NOT NULL,
    updated_at       BIGINT      NOT NULL,
    version          BIGINT      NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
CREATE INDEX IF NOT EXISTS idx_identity_grant_user ON identity_authorization_grant(user_id);
CREATE INDEX IF NOT EXISTS idx_identity_grant_client ON identity_authorization_grant(client_id);