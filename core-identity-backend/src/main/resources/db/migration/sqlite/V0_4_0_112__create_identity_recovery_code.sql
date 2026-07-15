-- V0.4.0.112: Create identity_recovery_code table (SQLite)
CREATE TABLE IF NOT EXISTS identity_recovery_code (
    id          TEXT    NOT NULL,
    code_set_id TEXT    NOT NULL,
    code_hash   TEXT    NOT NULL,
    status      TEXT    NOT NULL DEFAULT 'ACTIVE',
    used_at     INTEGER,
    created_at  INTEGER NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_rc_set ON identity_recovery_code(code_set_id);
CREATE INDEX idx_identity_rc_hash ON identity_recovery_code(code_hash);