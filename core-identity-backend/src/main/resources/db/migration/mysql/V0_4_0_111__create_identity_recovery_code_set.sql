-- V0.4.0.111: Create identity_recovery_code_set table
CREATE TABLE IF NOT EXISTS identity_recovery_code_set (
    id              VARCHAR(36) NOT NULL,
    user_id         VARCHAR(36) NOT NULL,
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    total_count     INTEGER     NOT NULL,
    remaining_count INTEGER     NOT NULL,
    generated_at    BIGINT      NOT NULL,
    revoked_at      BIGINT,
    version         BIGINT      NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_rcs_user_status ON identity_recovery_code_set(user_id, status);