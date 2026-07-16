CREATE TABLE IF NOT EXISTS identity_federation_certificate (
    id                    VARCHAR(36)  NOT NULL,
    connection_id         VARCHAR(36)  NOT NULL,
    certificate_type      VARCHAR(30)  NOT NULL,
    certificate_pem       TEXT,
    encrypted_private_key TEXT,
    key_version           VARCHAR(50),
    fingerprint           VARCHAR(128),
    status                VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    valid_from            BIGINT,
    valid_until           BIGINT,
    created_at            BIGINT       NOT NULL,
    updated_at            BIGINT       NOT NULL,
    version               BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_fed_cert_conn ON identity_federation_certificate(connection_id);
CREATE INDEX idx_identity_fed_cert_status ON identity_federation_certificate(status);
