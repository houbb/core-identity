-- V0.2.0.003: Create identity_credential table
CREATE TABLE IF NOT EXISTS identity_credential (
    id                   VARCHAR(36)  NOT NULL PRIMARY KEY,
    user_id              VARCHAR(36)  NOT NULL,
    credential_type      VARCHAR(30)  NOT NULL DEFAULT 'PASSWORD',
    secret_hash          VARCHAR(255) NOT NULL,
    algorithm            VARCHAR(30)  NOT NULL DEFAULT 'BCRYPT',
    status               VARCHAR(30)  NOT NULL DEFAULT 'ACTIVE',
    must_change          INTEGER      NOT NULL DEFAULT 0,
    password_changed_at  BIGINT,
    failed_attempt_count INTEGER      NOT NULL DEFAULT 0,
    created_at           BIGINT       NOT NULL,
    updated_at           BIGINT       NOT NULL,
    version              BIGINT       NOT NULL DEFAULT 1
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_credential_user_type
    ON identity_credential(user_id, credential_type);
CREATE INDEX IF NOT EXISTS idx_identity_credential_user
    ON identity_credential(user_id);