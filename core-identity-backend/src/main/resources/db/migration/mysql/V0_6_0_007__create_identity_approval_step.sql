CREATE TABLE IF NOT EXISTS identity_approval_step (
    id                   VARCHAR(36) NOT NULL,
    approval_instance_id VARCHAR(36) NOT NULL,
    step_order           INTEGER     NOT NULL,
    approval_mode        VARCHAR(30) NOT NULL,
    required_approvals   INTEGER     NOT NULL DEFAULT 1,
    approver_type        VARCHAR(30) NOT NULL,
    approver_reference   VARCHAR(255),
    status               VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    due_at               BIGINT,
    created_at           BIGINT      NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_approval_step_inst ON identity_approval_step(approval_instance_id);
CREATE INDEX idx_identity_approval_step_status ON identity_approval_step(status);
