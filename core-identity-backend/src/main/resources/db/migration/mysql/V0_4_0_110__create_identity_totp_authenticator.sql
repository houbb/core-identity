-- V0.4.0.110: Create identity_totp_authenticator table
CREATE TABLE IF NOT EXISTS identity_totp_authenticator (
    authenticator_id       VARCHAR(36) NOT NULL,
    encrypted_secret       TEXT        NOT NULL,
    encryption_key_version VARCHAR(50) NOT NULL,
    algorithm              VARCHAR(20) NOT NULL DEFAULT 'SHA1',
    digits                 INTEGER     NOT NULL DEFAULT 6,
    period_seconds         INTEGER     NOT NULL DEFAULT 30,
    last_accepted_step     BIGINT      NOT NULL DEFAULT 0,
    confirmed_at           BIGINT,
    PRIMARY KEY (authenticator_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;