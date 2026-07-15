-- V0.2.0.002: Create identity_user_email table
CREATE TABLE IF NOT EXISTS identity_user_email (
    id               VARCHAR(36)  NOT NULL PRIMARY KEY,
    user_id          VARCHAR(36)  NOT NULL,
    email_normalized VARCHAR(320) NOT NULL,
    email_display    VARCHAR(320) NOT NULL,
    is_primary       INTEGER      NOT NULL DEFAULT 1,
    verified_at      BIGINT,
    created_at       BIGINT       NOT NULL,
    updated_at       BIGINT       NOT NULL,
    version          BIGINT       NOT NULL DEFAULT 1
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_user_email_normalized
    ON identity_user_email(email_normalized);
CREATE INDEX IF NOT EXISTS idx_identity_user_email_user
    ON identity_user_email(user_id);
CREATE INDEX IF NOT EXISTS idx_identity_user_email_verified
    ON identity_user_email(verified_at);