CREATE TABLE IF NOT EXISTS identity_access_review_decision (
    id                         TEXT    NOT NULL,
    review_item_id             TEXT    NOT NULL,
    reviewer_user_id           TEXT    NOT NULL,
    decision                   TEXT    NOT NULL,
    reason                     TEXT,
    new_expiry_at              INTEGER,
    replacement_entitlement_id TEXT,
    decided_at                 INTEGER NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_review_dec_item ON identity_access_review_decision(review_item_id);
CREATE INDEX idx_identity_review_dec_reviewer ON identity_access_review_decision(reviewer_user_id);