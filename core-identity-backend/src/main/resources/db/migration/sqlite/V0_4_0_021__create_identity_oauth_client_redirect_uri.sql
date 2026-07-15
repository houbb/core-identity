-- V0.4.0.021: Create identity_oauth_client_redirect_uri table
CREATE TABLE IF NOT EXISTS identity_oauth_client_redirect_uri (
    id           VARCHAR(36)   NOT NULL PRIMARY KEY,
    client_id    VARCHAR(36)   NOT NULL,
    redirect_uri VARCHAR(1000) NOT NULL,
    environment  VARCHAR(20)   DEFAULT 'PRODUCTION',
    status       VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    created_at   BIGINT        NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_oauth_client_redirect_uri
    ON identity_oauth_client_redirect_uri(client_id, redirect_uri);