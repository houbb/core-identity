CREATE TABLE IF NOT EXISTS identity_federated_session (
    id                   VARCHAR(36)  NOT NULL,
    session_id           VARCHAR(36)  NOT NULL,
    connection_id        VARCHAR(36)  NOT NULL,
    external_identity_id VARCHAR(36),
    upstream_session_id  VARCHAR(500),
    upstream_subject     VARCHAR(500),
    upstream_auth_time   BIGINT,
    upstream_acr         VARCHAR(255),
    upstream_amr_json    TEXT,
    logout_status        VARCHAR(30)  NOT NULL DEFAULT 'NONE',
    created_at           BIGINT       NOT NULL,
    updated_at           BIGINT       NOT NULL,
    version              BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_fs_session ON identity_federated_session(session_id);
CREATE INDEX idx_identity_fs_conn ON identity_federated_session(connection_id);
