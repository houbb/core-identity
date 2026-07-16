CREATE TABLE IF NOT EXISTS identity_scim_client (
    id                TEXT    NOT NULL,
    organization_id   TEXT    NOT NULL,
    connection_id     TEXT    NOT NULL,
    name              TEXT,
    token_prefix      TEXT    NOT NULL,
    token_hash        TEXT    NOT NULL,
    scopes_json       TEXT,
    status            TEXT    NOT NULL DEFAULT 'ACTIVE',
    expires_at        INTEGER,
    ip_allowlist_json TEXT,
    last_used_at      INTEGER,
    last_used_ip      TEXT,
    created_at        INTEGER NOT NULL,
    updated_at        INTEGER NOT NULL,
    version           INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_sc_org ON identity_scim_client(organization_id);
CREATE INDEX idx_identity_sc_conn ON identity_scim_client(connection_id);
CREATE INDEX idx_identity_sc_prefix ON identity_scim_client(token_prefix);
