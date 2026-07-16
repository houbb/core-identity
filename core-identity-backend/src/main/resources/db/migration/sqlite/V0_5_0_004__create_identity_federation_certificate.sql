CREATE TABLE IF NOT EXISTS identity_federation_certificate (
    id                    TEXT    NOT NULL,
    connection_id         TEXT    NOT NULL,
    certificate_type      TEXT    NOT NULL,
    certificate_pem       TEXT,
    encrypted_private_key TEXT,
    key_version           TEXT,
    fingerprint           TEXT,
    status                TEXT    NOT NULL DEFAULT 'PENDING',
    valid_from            INTEGER,
    valid_until           INTEGER,
    created_at            INTEGER NOT NULL,
    updated_at            INTEGER NOT NULL,
    version               INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_fed_cert_conn ON identity_federation_certificate(connection_id);
CREATE INDEX idx_identity_fed_cert_status ON identity_federation_certificate(status);
