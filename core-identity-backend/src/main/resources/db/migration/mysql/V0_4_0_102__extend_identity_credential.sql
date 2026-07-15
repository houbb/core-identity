-- V0.4.0.102: Extend identity_credential with P4 columns
ALTER TABLE identity_credential
    ADD COLUMN hash_policy_version       VARCHAR(30),
    ADD COLUMN last_rehashed_at          BIGINT,
    ADD COLUMN compromised_detected_at   BIGINT;