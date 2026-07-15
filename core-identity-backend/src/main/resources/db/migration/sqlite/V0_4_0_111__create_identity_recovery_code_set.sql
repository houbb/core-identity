-- V0.4.0.111: Create identity_recovery_code_set table (SQLite)
CREATE TABLE IF NOT EXISTS identity_recovery_code_set (
    id              TEXT    NOT NULL,
    user_id         TEXT    NOT NULL,
    status          TEXT    NOT NULL DEFAULT 'ACTIVE',
    total_count     INTEGER NOT NULL,
    remaining_count INTEGER NOT NULL,
    generated_at    INTEGER NOT NULL,
    revoked_at      INTEGER,
    version         INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_rcs_user_status ON identity_recovery_code_set(user_id, status);