-- V0.4.0.150: Create identity_account_recovery table
CREATE TABLE IF NOT EXISTS identity_account_recovery (
    id                      VARCHAR(36) NOT NULL,
    user_id                 VARCHAR(36) NOT NULL,
    recovery_type           VARCHAR(40) NOT NULL,
    status                  VARCHAR(30) NOT NULL DEFAULT 'PENDING_VERIFICATION',
    risk_level              VARCHAR(20),
    required_evidence_level VARCHAR(30),
    initiated_ip            VARCHAR(64),
    initiated_device_id     VARCHAR(36),
    cooling_off_until       BIGINT,
    approved_by             VARCHAR(36),
    rejected_by             VARCHAR(36),
    completed_at            BIGINT,
    cancelled_at            BIGINT,
    created_at              BIGINT      NOT NULL,
    updated_at              BIGINT      NOT NULL,
    version                 BIGINT      NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_recovery_user ON identity_account_recovery(user_id, status);
CREATE INDEX idx_identity_recovery_status ON identity_account_recovery(status);