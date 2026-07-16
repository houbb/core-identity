CREATE TABLE IF NOT EXISTS identity_account_link_request (
    id                  TEXT    NOT NULL,
    connection_id       TEXT    NOT NULL,
    external_subject    TEXT    NOT NULL,
    candidate_user_id   TEXT,
    external_email      TEXT,
    status              TEXT    NOT NULL DEFAULT 'PENDING',
    risk_level          TEXT    NOT NULL DEFAULT 'MEDIUM',
    verification_method TEXT,
    expires_at          INTEGER,
    confirmed_at        INTEGER,
    rejected_at         INTEGER,
    created_at          INTEGER NOT NULL,
    updated_at          INTEGER NOT NULL,
    version             INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_alr_conn ON identity_account_link_request(connection_id);
CREATE INDEX idx_identity_alr_status ON identity_account_link_request(status);
