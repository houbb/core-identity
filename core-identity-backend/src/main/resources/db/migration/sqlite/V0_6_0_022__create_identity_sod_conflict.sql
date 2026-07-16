CREATE TABLE IF NOT EXISTS identity_sod_conflict (
    id             TEXT    NOT NULL,
    policy_id      TEXT    NOT NULL,
    subject_id     TEXT    NOT NULL,
    left_grant_id  TEXT    NOT NULL,
    right_grant_id TEXT    NOT NULL,
    status         TEXT    NOT NULL DEFAULT 'OPEN',
    detected_at    INTEGER NOT NULL,
    resolved_at    INTEGER,
    resolution     TEXT,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_sod_conflict_policy ON identity_sod_conflict(policy_id);
CREATE INDEX idx_identity_sod_conflict_subject ON identity_sod_conflict(subject_id);
CREATE INDEX idx_identity_sod_conflict_status ON identity_sod_conflict(status);