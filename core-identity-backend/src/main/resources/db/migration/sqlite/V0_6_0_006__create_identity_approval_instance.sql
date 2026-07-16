CREATE TABLE IF NOT EXISTS identity_approval_instance (
    id              TEXT    NOT NULL,
    request_type    TEXT    NOT NULL,
    request_id      TEXT    NOT NULL,
    status          TEXT    NOT NULL DEFAULT 'PENDING',
    current_step    INTEGER NOT NULL DEFAULT 0,
    created_at      INTEGER NOT NULL,
    updated_at      INTEGER NOT NULL DEFAULT 0,
    completed_at    INTEGER,
    version         INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_approval_inst_req ON identity_approval_instance(request_type, request_id);
CREATE INDEX idx_identity_approval_inst_status ON identity_approval_instance(status);
