-- V0.4.0.043: Create identity_service_credential table (SQLite)
CREATE TABLE IF NOT EXISTS identity_service_credential (
    id                  TEXT    NOT NULL PRIMARY KEY,
    service_account_id  TEXT    NOT NULL,
    client_id           TEXT    NOT NULL,
    secret_prefix       TEXT    NOT NULL,
    secret_hash         TEXT    NOT NULL,
    name                TEXT    NOT NULL,
    status              TEXT    NOT NULL DEFAULT 'ACTIVE',
    expires_at          INTEGER,
    last_used_at        INTEGER,
    created_at          INTEGER NOT NULL,
    revoked_at          INTEGER
);

CREATE INDEX IF NOT EXISTS idx_identity_sc_sa ON identity_service_credential(service_account_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_sc_client_id ON identity_service_credential(client_id);
CREATE INDEX IF NOT EXISTS idx_identity_sc_status ON identity_service_credential(status);
