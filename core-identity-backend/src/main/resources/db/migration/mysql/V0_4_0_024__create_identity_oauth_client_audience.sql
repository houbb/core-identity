-- V0.4.0.024: Create identity_oauth_client_audience table
CREATE TABLE IF NOT EXISTS identity_oauth_client_audience (
    id          VARCHAR(36) NOT NULL PRIMARY KEY,
    client_id   VARCHAR(36) NOT NULL,
    audience_id VARCHAR(36) NOT NULL,
    created_at  BIGINT      NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_oauth_client_audience
    ON identity_oauth_client_audience(client_id, audience_id);