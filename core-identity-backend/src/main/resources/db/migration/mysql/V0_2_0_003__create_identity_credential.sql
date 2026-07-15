-- V0.2.0.003: Create identity_credential table
CREATE TABLE IF NOT EXISTS identity_credential (
    id                   VARCHAR(36)  NOT NULL,
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
    version              BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX uq_identity_credential_user_type ON identity_credential(user_id, credential_type);
CREATE INDEX idx_identity_credential_user ON identity_credential(user_id);