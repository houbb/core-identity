CREATE TABLE IF NOT EXISTS identity_access_review_item (
    id               TEXT    NOT NULL,
    campaign_id      TEXT    NOT NULL,
    subject_type     TEXT    NOT NULL,
    subject_id       TEXT    NOT NULL,
    entitlement_id   TEXT    NOT NULL,
    grant_id         TEXT,
    reviewer_user_id TEXT,
    risk_level       TEXT    NOT NULL DEFAULT 'LOW',
    last_used_at     INTEGER,
    status           TEXT    NOT NULL DEFAULT 'PENDING',
    created_at       INTEGER NOT NULL,
    updated_at       INTEGER NOT NULL,
    version          INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_review_item_camp ON identity_access_review_item(campaign_id);
CREATE INDEX idx_identity_review_item_subject ON identity_access_review_item(subject_id);
CREATE INDEX idx_identity_review_item_reviewer ON identity_access_review_item(reviewer_user_id);
CREATE INDEX idx_identity_review_item_status ON identity_access_review_item(status);