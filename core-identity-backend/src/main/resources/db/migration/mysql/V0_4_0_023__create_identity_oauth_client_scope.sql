-- V0.4.0.023: Create identity_oauth_client_scope table
CREATE TABLE IF NOT EXISTS identity_oauth_client_scope (
    id         VARCHAR(36) NOT NULL PRIMARY KEY,
    client_id  VARCHAR(36) NOT NULL,
    scope_id   VARCHAR(36) NOT NULL,
    created_at BIGINT      NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_oauth_client_scope
    ON identity_oauth_client_scope(client_id, scope_id);