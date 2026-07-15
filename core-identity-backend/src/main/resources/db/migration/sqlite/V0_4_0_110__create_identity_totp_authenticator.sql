-- V0.4.0.110: Create identity_totp_authenticator table (SQLite)
CREATE TABLE IF NOT EXISTS identity_totp_authenticator (
    authenticator_id       TEXT    NOT NULL,
    encrypted_secret       TEXT    NOT NULL,
    encryption_key_version TEXT    NOT NULL,
    algorithm              TEXT    NOT NULL DEFAULT 'SHA1',
    digits                 INTEGER NOT NULL DEFAULT 6,
    period_seconds         INTEGER NOT NULL DEFAULT 30,
    last_accepted_step     INTEGER NOT NULL DEFAULT 0,
    confirmed_at           INTEGER,
    PRIMARY KEY (authenticator_id)
);