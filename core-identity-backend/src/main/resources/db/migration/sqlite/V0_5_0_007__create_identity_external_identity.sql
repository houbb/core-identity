CREATE TABLE IF NOT EXISTS identity_external_identity (
    id                   TEXT    NOT NULL,
    user_id              TEXT,
    organization_id      TEXT    NOT NULL,
    connection_id        TEXT    NOT NULL,
    external_subject     TEXT    NOT NULL,
    external_username    TEXT,
    external_email       TEXT,
    external_employee_id TEXT,
    status               TEXT    NOT NULL DEFAULT 'ACTIVE',
    management_source    TEXT,
    claims_snapshot_json TEXT,
    first_login_at       INTEGER,
    last_login_at        INTEGER,
    linked_at            INTEGER,
    unlinked_at          INTEGER,
    created_at           INTEGER NOT NULL,
    updated_at           INTEGER NOT NULL,
    version              INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_identity_ei_conn_sub ON identity_external_identity(connection_id, external_subject);
CREATE INDEX idx_identity_ei_user ON identity_external_identity(user_id);
CREATE INDEX idx_identity_ei_org ON identity_external_identity(organization_id);