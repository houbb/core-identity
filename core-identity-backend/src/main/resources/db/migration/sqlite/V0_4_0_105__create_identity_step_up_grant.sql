-- V0.4.0.105: Create identity_step_up_grant table (SQLite)
CREATE TABLE IF NOT EXISTS identity_step_up_grant (
    id                   TEXT    NOT NULL,
    user_id              TEXT    NOT NULL,
    session_id           TEXT    NOT NULL,
    authentication_level TEXT    NOT NULL,
    allowed_actions_json TEXT,
    status               TEXT    NOT NULL DEFAULT 'ACTIVE',
    issued_at            INTEGER NOT NULL,
    expires_at           INTEGER NOT NULL,
    consumed_at          INTEGER,
    created_at           INTEGER NOT NULL,
    version              INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_step_up_user_session ON identity_step_up_grant(user_id, session_id);
CREATE INDEX idx_identity_step_up_expires ON identity_step_up_grant(expires_at);