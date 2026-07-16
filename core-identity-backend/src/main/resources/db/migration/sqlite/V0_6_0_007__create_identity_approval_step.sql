CREATE TABLE IF NOT EXISTS identity_approval_step (
    id                   TEXT    NOT NULL,
    approval_instance_id TEXT    NOT NULL,
    step_order           INTEGER NOT NULL,
    approval_mode        TEXT    NOT NULL,
    required_approvals   INTEGER NOT NULL DEFAULT 1,
    approver_type        TEXT    NOT NULL,
    approver_reference   TEXT,
    status               TEXT    NOT NULL DEFAULT 'PENDING',
    due_at               INTEGER,
    created_at           INTEGER NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_approval_step_inst ON identity_approval_step(approval_instance_id);
CREATE INDEX idx_identity_approval_step_status ON identity_approval_step(status);
