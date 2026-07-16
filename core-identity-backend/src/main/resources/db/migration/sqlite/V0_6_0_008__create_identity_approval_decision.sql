CREATE TABLE IF NOT EXISTS identity_approval_decision (
    id                    TEXT    NOT NULL,
    approval_step_id      TEXT    NOT NULL,
    approver_user_id      TEXT    NOT NULL,
    decision              TEXT    NOT NULL,
    reason                TEXT,
    decided_at            INTEGER NOT NULL,
    authentication_level  TEXT,
    request_id            TEXT,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_approval_dec_step ON identity_approval_decision(approval_step_id);
CREATE INDEX idx_identity_approval_dec_approver ON identity_approval_decision(approver_user_id);
CREATE INDEX idx_identity_approval_dec_request ON identity_approval_decision(request_id);
