CREATE TABLE IF NOT EXISTS identity_domain_verification (
    id                   TEXT    NOT NULL,
    domain_id            TEXT    NOT NULL,
    challenge_hash       TEXT,
    expected_record_name TEXT,
    method               TEXT    NOT NULL DEFAULT 'DNS_TXT',
    status               TEXT    NOT NULL DEFAULT 'PENDING',
    attempt_count        INTEGER NOT NULL DEFAULT 0,
    expires_at           INTEGER,
    verified_at          INTEGER,
    created_at           INTEGER NOT NULL,
    updated_at           INTEGER NOT NULL,
    version              INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_dv_domain ON identity_domain_verification(domain_id);
