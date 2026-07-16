CREATE TABLE IF NOT EXISTS identity_sod_exception (
    id                    VARCHAR(36)   NOT NULL,
    conflict_id           VARCHAR(36)   NOT NULL,
    reason                VARCHAR(2000) NOT NULL,
    compensating_control  VARCHAR(2000),
    approved_by           VARCHAR(36),
    valid_from            BIGINT        NOT NULL,
    expires_at            BIGINT        NOT NULL,
    review_at             BIGINT,
    status                VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_sod_exc_conflict ON identity_sod_exception(conflict_id);
CREATE INDEX idx_identity_sod_exc_status ON identity_sod_exception(status);
CREATE INDEX idx_identity_sod_exc_expires ON identity_sod_exception(expires_at);