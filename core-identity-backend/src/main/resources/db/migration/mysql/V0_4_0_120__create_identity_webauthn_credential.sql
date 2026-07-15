-- V0.4.0.120: Create identity_webauthn_credential table
CREATE TABLE IF NOT EXISTS identity_webauthn_credential (
    authenticator_id   VARCHAR(36)  NOT NULL,
    credential_id      TEXT         NOT NULL,
    public_key         TEXT         NOT NULL,
    user_handle        VARCHAR(255),
    sign_count         BIGINT       NOT NULL DEFAULT 0,
    aaguid             VARCHAR(100),
    transports_json    TEXT,
    attachment         VARCHAR(30),
    discoverable       INTEGER      NOT NULL DEFAULT 0,
    backup_eligible    INTEGER      NOT NULL DEFAULT 0,
    backup_state       INTEGER      NOT NULL DEFAULT 0,
    attestation_format VARCHAR(50),
    created_origin     VARCHAR(500),
    rp_id              VARCHAR(255),
    created_at         BIGINT       NOT NULL,
    last_used_at       BIGINT,
    PRIMARY KEY (authenticator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX uq_identity_webauthn_cred_id ON identity_webauthn_credential(credential_id(255));