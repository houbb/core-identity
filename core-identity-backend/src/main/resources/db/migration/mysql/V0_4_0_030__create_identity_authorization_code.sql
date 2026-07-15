-- V0.4.0.030: Create identity_authorization_code table
CREATE TABLE IF NOT EXISTS identity_authorization_code (
    id                    VARCHAR(36)   NOT NULL PRIMARY KEY,
    code_hash             VARCHAR(255)  NOT NULL,
    client_id             VARCHAR(36)   NOT NULL,
    user_id               VARCHAR(36),
    organization_id       VARCHAR(36),
    redirect_uri          VARCHAR(1000) NOT NULL,
    audience              VARCHAR(120),
    scopes_json           TEXT,
    code_challenge        VARCHAR(255),
    code_challenge_method VARCHAR(20)   DEFAULT 'S256',
    nonce                 VARCHAR(255),
    status                VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    expires_at            BIGINT        NOT NULL,
    used_at               BIGINT,
    created_at            BIGINT        NOT NULL,
    version               BIGINT        NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_auth_code_hash ON identity_authorization_code(code_hash);
CREATE INDEX IF NOT EXISTS idx_identity_auth_code_expires ON identity_authorization_code(expires_at);