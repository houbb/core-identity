-- V0.4.0.104: Create identity_authentication_challenge table (SQLite)
CREATE TABLE IF NOT EXISTS identity_authentication_challenge (
    id                   TEXT    NOT NULL,
    user_id              TEXT,
    session_id           TEXT,
    challenge_type       TEXT    NOT NULL,
    required_level       TEXT,
    allowed_methods_json TEXT,
    challenge_hash       TEXT,
    context_json         TEXT,
    status               TEXT    NOT NULL DEFAULT 'PENDING',
    attempt_count        INTEGER NOT NULL DEFAULT 0,
    expires_at           INTEGER NOT NULL,
    completed_at         INTEGER,
    created_at           INTEGER NOT NULL,
    version              INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_challenge_user ON identity_authentication_challenge(user_id);
CREATE INDEX idx_identity_challenge_session ON identity_authentication_challenge(session_id);
CREATE INDEX idx_identity_challenge_status ON identity_authentication_challenge(status, expires_at);