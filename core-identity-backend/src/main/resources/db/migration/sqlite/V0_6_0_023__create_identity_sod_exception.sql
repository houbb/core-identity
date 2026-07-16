CREATE TABLE IF NOT EXISTS identity_sod_exception (
    id                    TEXT    NOT NULL,
    conflict_id           TEXT    NOT NULL,
    reason                TEXT    NOT NULL,
    compensating_control  TEXT,
    approved_by           TEXT,
    valid_from            INTEGER NOT NULL,
    expires_at            INTEGER NOT NULL,
    review_at             INTEGER,
    status                TEXT    NOT NULL DEFAULT 'ACTIVE',
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_sod_exc_conflict ON identity_sod_exception(conflict_id);
CREATE INDEX idx_identity_sod_exc_status ON identity_sod_exception(status);
CREATE INDEX idx_identity_sod_exc_expires ON identity_sod_exception(expires_at);