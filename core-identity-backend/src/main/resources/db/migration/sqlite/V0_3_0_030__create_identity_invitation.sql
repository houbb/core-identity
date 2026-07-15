-- V0.3.0.030: Create identity_invitation table
CREATE TABLE IF NOT EXISTS identity_invitation (
    id                   VARCHAR(36)  NOT NULL PRIMARY KEY,
    organization_id      VARCHAR(36)  NOT NULL,
    email_normalized     VARCHAR(320) NOT NULL,
    email_display        VARCHAR(320),
    token_hash           VARCHAR(255) NOT NULL,
    status               VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    invited_by_user_id   VARCHAR(36),
    accepted_by_user_id  VARCHAR(36),
    message              VARCHAR(500),
    expires_at           BIGINT       NOT NULL,
    accepted_at          BIGINT,
    declined_at          BIGINT,
    revoked_at           BIGINT,
    created_at           BIGINT       NOT NULL,
    updated_at           BIGINT       NOT NULL,
    version              BIGINT       NOT NULL DEFAULT 1
);

CREATE INDEX IF NOT EXISTS idx_identity_invitation_org_status
    ON identity_invitation(organization_id, status);
CREATE INDEX IF NOT EXISTS idx_identity_invitation_email_status
    ON identity_invitation(email_normalized, status);
CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_invitation_token_hash
    ON identity_invitation(token_hash);
CREATE INDEX IF NOT EXISTS idx_identity_invitation_expires
    ON identity_invitation(expires_at);