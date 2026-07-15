-- V0.4.0.103: Create identity_authenticator table (SQLite)
CREATE TABLE IF NOT EXISTS identity_authenticator (
    id                        TEXT    NOT NULL,
    user_id                   TEXT    NOT NULL,
    authenticator_type        TEXT    NOT NULL,
    name                      TEXT,
    status                    TEXT    NOT NULL DEFAULT 'PENDING',
    assurance_level           TEXT    NOT NULL DEFAULT 'AUTH_LEVEL_1',
    phishing_resistant        INTEGER NOT NULL DEFAULT 0,
    user_verification_capable INTEGER NOT NULL DEFAULT 0,
    enrolled_at               INTEGER,
    last_used_at              INTEGER,
    compromised_at            INTEGER,
    revoked_at                INTEGER,
    created_at                INTEGER NOT NULL,
    updated_at                INTEGER NOT NULL,
    version                   INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_auth_user_status ON identity_authenticator(user_id, status);
CREATE INDEX idx_identity_auth_user_type ON identity_authenticator(user_id, authenticator_type);
CREATE INDEX idx_identity_auth_last_used ON identity_authenticator(last_used_at);
