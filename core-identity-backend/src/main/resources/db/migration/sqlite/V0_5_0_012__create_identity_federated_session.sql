CREATE TABLE IF NOT EXISTS identity_federated_session (
    id                   TEXT    NOT NULL,
    session_id           TEXT    NOT NULL,
    connection_id        TEXT    NOT NULL,
    external_identity_id TEXT,
    upstream_session_id  TEXT,
    upstream_subject     TEXT,
    upstream_auth_time   INTEGER,
    upstream_acr         TEXT,
    upstream_amr_json    TEXT,
    logout_status        TEXT    NOT NULL DEFAULT 'NONE',
    created_at           INTEGER NOT NULL,
    updated_at           INTEGER NOT NULL,
    version              INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_fs_session ON identity_federated_session(session_id);
CREATE INDEX idx_identity_fs_conn ON identity_federated_session(connection_id);
