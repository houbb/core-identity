-- V0.4.0.020: Create identity_oauth_client table
CREATE TABLE IF NOT EXISTS identity_oauth_client (
    id                 VARCHAR(36)   NOT NULL PRIMARY KEY,
    client_id          VARCHAR(120)  NOT NULL,
    owner_type         VARCHAR(30)   NOT NULL DEFAULT 'USER',
    owner_id           VARCHAR(36)   NOT NULL,
    client_type        VARCHAR(30)   NOT NULL DEFAULT 'CONFIDENTIAL',
    name               VARCHAR(150)  NOT NULL,
    description        VARCHAR(1000),
    homepage_url       VARCHAR(500),
    logo_object_id     VARCHAR(36),
    privacy_policy_url VARCHAR(500),
    terms_url          VARCHAR(500),
    status             VARCHAR(30)   NOT NULL DEFAULT 'DRAFT',
    review_status      VARCHAR(30),
    consent_required   INTEGER       NOT NULL DEFAULT 1,
    created_by         VARCHAR(36),
    created_at         BIGINT        NOT NULL,
    updated_at         BIGINT        NOT NULL,
    version            BIGINT        NOT NULL DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_oauth_client_client_id
    ON identity_oauth_client(client_id);
CREATE INDEX IF NOT EXISTS idx_identity_oauth_client_owner
    ON identity_oauth_client(owner_type, owner_id);
CREATE INDEX IF NOT EXISTS idx_identity_oauth_client_status
    ON identity_oauth_client(status);