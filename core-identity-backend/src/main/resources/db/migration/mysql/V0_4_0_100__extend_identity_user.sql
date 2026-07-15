-- V0.4.0.100: Extend identity_user with P4 security columns
ALTER TABLE identity_user
    ADD COLUMN security_version            BIGINT  NOT NULL DEFAULT 1,
    ADD COLUMN security_status             VARCHAR(30) NOT NULL DEFAULT 'BASIC',
    ADD COLUMN risk_level                  VARCHAR(20) NOT NULL DEFAULT 'LOW',
    ADD COLUMN mfa_enrolled                INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN phishing_resistant_enrolled INTEGER NOT NULL DEFAULT 0,
    ADD COLUMN recovery_state              VARCHAR(30),
    ADD COLUMN last_security_review_at     BIGINT;

CREATE INDEX idx_identity_user_risk_level ON identity_user(risk_level);
CREATE INDEX idx_identity_user_security_status ON identity_user(security_status);
