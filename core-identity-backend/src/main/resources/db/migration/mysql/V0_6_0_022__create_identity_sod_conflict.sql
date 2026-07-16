CREATE TABLE IF NOT EXISTS identity_sod_conflict (
    id             VARCHAR(36) NOT NULL,
    policy_id      VARCHAR(36) NOT NULL,
    subject_id     VARCHAR(36) NOT NULL,
    left_grant_id  VARCHAR(36) NOT NULL,
    right_grant_id VARCHAR(36) NOT NULL,
    status         VARCHAR(30) NOT NULL DEFAULT 'OPEN',
    detected_at    BIGINT      NOT NULL,
    resolved_at    BIGINT,
    resolution     VARCHAR(30),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_sod_conflict_policy ON identity_sod_conflict(policy_id);
CREATE INDEX idx_identity_sod_conflict_subject ON identity_sod_conflict(subject_id);
CREATE INDEX idx_identity_sod_conflict_status ON identity_sod_conflict(status);