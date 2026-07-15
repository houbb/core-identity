-- V0.4.0.042: Create identity_service_account_role table (SQLite)
CREATE TABLE IF NOT EXISTS identity_service_account_role (
    id                  TEXT    NOT NULL PRIMARY KEY,
    service_account_id  TEXT    NOT NULL,
    role_id             TEXT    NOT NULL,
    assigned_by         TEXT,
    created_at          INTEGER NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_identity_sar_sa ON identity_service_account_role(service_account_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_identity_sar_sa_role ON identity_service_account_role(service_account_id, role_id);
