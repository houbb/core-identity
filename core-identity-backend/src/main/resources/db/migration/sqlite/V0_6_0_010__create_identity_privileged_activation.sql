CREATE TABLE IF NOT EXISTS identity_privileged_activation (
    id                    TEXT    NOT NULL,
    grant_id              TEXT,
    user_id               TEXT    NOT NULL,
    organization_id       TEXT,
    role_id               TEXT,
    reason                TEXT,
    ticket_reference      TEXT,
    status                TEXT    NOT NULL DEFAULT 'ACTIVE',
    authentication_level  TEXT,
    session_id            TEXT,
    activated_at          INTEGER NOT NULL,
    expires_at            INTEGER NOT NULL,
    ended_at              INTEGER,
    created_at            INTEGER NOT NULL,
    updated_at            INTEGER NOT NULL,
    version               INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_priv_act_user ON identity_privileged_activation(user_id);
CREATE INDEX idx_identity_priv_act_grant ON identity_privileged_activation(grant_id);
CREATE INDEX idx_identity_priv_act_status ON identity_privileged_activation(status);
CREATE INDEX idx_identity_priv_act_expires ON identity_privileged_activation(expires_at);