CREATE TABLE IF NOT EXISTS identity_approval_decision (
    id                    VARCHAR(36)  NOT NULL,
    approval_step_id      VARCHAR(36)  NOT NULL,
    approver_user_id      VARCHAR(36)  NOT NULL,
    decision              VARCHAR(30)  NOT NULL,
    reason                VARCHAR(1000),
    decided_at            BIGINT       NOT NULL,
    authentication_level  VARCHAR(30),
    request_id            VARCHAR(36),
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_approval_dec_step ON identity_approval_decision(approval_step_id);
CREATE INDEX idx_identity_approval_dec_approver ON identity_approval_decision(approver_user_id);
CREATE INDEX idx_identity_approval_dec_request ON identity_approval_decision(request_id);
