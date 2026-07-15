-- V0.4.0.120: Create identity_webauthn_credential table (SQLite)
CREATE TABLE IF NOT EXISTS identity_webauthn_credential (
    authenticator_id   TEXT    NOT NULL,
    credential_id      TEXT    NOT NULL,
    public_key         TEXT    NOT NULL,
    user_handle        TEXT,
    sign_count         INTEGER NOT NULL DEFAULT 0,
    aaguid             TEXT,
    transports_json    TEXT,
    attachment         TEXT,
    discoverable       INTEGER NOT NULL DEFAULT 0,
    backup_eligible    INTEGER NOT NULL DEFAULT 0,
    backup_state       INTEGER NOT NULL DEFAULT 0,
    attestation_format TEXT,
    created_origin     TEXT,
    rp_id              TEXT,
    created_at         INTEGER NOT NULL,
    last_used_at       INTEGER,
    PRIMARY KEY (authenticator_id)
);

CREATE UNIQUE INDEX uq_identity_webauthn_cred_id ON identity_webauthn_credential(credential_id);