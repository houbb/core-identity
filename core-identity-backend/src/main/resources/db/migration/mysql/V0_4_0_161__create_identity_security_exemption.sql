-- V0.4.0.161: Create identity_security_exemption table
CREATE TABLE IF NOT EXISTS identity_security_exemption (
    id            VARCHAR(36)  NOT NULL,
    policy_id     VARCHAR(36)  NOT NULL,
    membership_id VARCHAR(36)  NOT NULL,
    reason        VARCHAR(500),
    status        VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    expires_at    BIGINT       NOT NULL,
    granted_by    VARCHAR(36),
    created_at    BIGINT       NOT NULL,
    revoked_at    BIGINT,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_exemption_policy ON identity_security_exemption(policy_id);
CREATE INDEX idx_identity_exemption_member ON identity_security_exemption(membership_id);