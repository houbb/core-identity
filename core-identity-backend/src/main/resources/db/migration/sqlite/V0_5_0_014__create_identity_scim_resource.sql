CREATE TABLE IF NOT EXISTS identity_scim_resource (
    id                TEXT    NOT NULL,
    connection_id     TEXT    NOT NULL,
    resource_type     TEXT    NOT NULL,
    external_id       TEXT    NOT NULL,
    local_resource_id TEXT,
    user_name         TEXT,
    active            INTEGER NOT NULL DEFAULT 1,
    resource_version  INTEGER NOT NULL DEFAULT 1,
    last_payload_hash TEXT,
    last_synced_at    INTEGER,
    created_at        INTEGER NOT NULL,
    updated_at        INTEGER NOT NULL,
    version           INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_identity_sr_conn_ext ON identity_scim_resource(connection_id, resource_type, external_id);
CREATE INDEX idx_identity_sr_local ON identity_scim_resource(local_resource_id);
