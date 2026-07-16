CREATE TABLE IF NOT EXISTS identity_approval_instance (
    id              VARCHAR(36) NOT NULL,
    request_type    VARCHAR(40) NOT NULL,
    request_id      VARCHAR(36) NOT NULL,
    status          VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    current_step    INTEGER     NOT NULL DEFAULT 0,
    created_at      BIGINT      NOT NULL,
    updated_at      BIGINT      NOT NULL DEFAULT 0,
    completed_at    BIGINT,
    version         BIGINT      NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_approval_inst_req ON identity_approval_instance(request_type, request_id);
CREATE INDEX idx_identity_approval_inst_status ON identity_approval_instance(status);
