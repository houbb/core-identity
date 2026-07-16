CREATE TABLE IF NOT EXISTS identity_scim_group (
    id               TEXT    NOT NULL,
    connection_id    TEXT    NOT NULL,
    scim_resource_id TEXT,
    external_id      TEXT,
    display_name     TEXT    NOT NULL,
    status           TEXT    NOT NULL DEFAULT 'ACTIVE',
    created_at       INTEGER NOT NULL,
    updated_at       INTEGER NOT NULL,
    version          INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_sg_conn ON identity_scim_group(connection_id);
