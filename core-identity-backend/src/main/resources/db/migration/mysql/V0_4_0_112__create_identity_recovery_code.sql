-- V0.4.0.112: Create identity_recovery_code table
CREATE TABLE IF NOT EXISTS identity_recovery_code (
    id          VARCHAR(36)  NOT NULL,
    code_set_id VARCHAR(36)  NOT NULL,
    code_hash   VARCHAR(255) NOT NULL,
    status      VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    used_at     BIGINT,
    created_at  BIGINT       NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_rc_set ON identity_recovery_code(code_set_id);
CREATE INDEX idx_identity_rc_hash ON identity_recovery_code(code_hash);