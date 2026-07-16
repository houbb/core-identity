CREATE TABLE IF NOT EXISTS identity_federation_connection (
    id                  TEXT    NOT NULL,
    connection_key      TEXT    NOT NULL,
    organization_id     TEXT    NOT NULL,
    connection_type     TEXT    NOT NULL,
    name                TEXT,
    status              TEXT    NOT NULL DEFAULT 'DRAFT',
    login_button_text   TEXT,
    logo_object_id      TEXT,
    priority            INTEGER NOT NULL DEFAULT 0,
    jit_enabled         INTEGER NOT NULL DEFAULT 0,
    scim_enabled        INTEGER NOT NULL DEFAULT 0,
    last_success_at     INTEGER,
    last_failure_at     INTEGER,
    last_error_code     TEXT,
    created_by          TEXT,
    created_at          INTEGER NOT NULL,
    updated_at          INTEGER NOT NULL,
    version             INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_identity_fed_conn_key ON identity_federation_connection(connection_key);
CREATE INDEX idx_identity_fed_conn_org ON identity_federation_connection(organization_id);
CREATE INDEX idx_identity_fed_conn_status ON identity_federation_connection(status);