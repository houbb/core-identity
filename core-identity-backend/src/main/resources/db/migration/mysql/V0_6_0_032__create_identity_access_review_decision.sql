CREATE TABLE IF NOT EXISTS identity_access_review_decision (
    id                         VARCHAR(36)  NOT NULL,
    review_item_id             VARCHAR(36)  NOT NULL,
    reviewer_user_id           VARCHAR(36)  NOT NULL,
    decision                   VARCHAR(30)  NOT NULL,
    reason                     VARCHAR(1000),
    new_expiry_at              BIGINT,
    replacement_entitlement_id VARCHAR(36),
    decided_at                 BIGINT       NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_review_dec_item ON identity_access_review_decision(review_item_id);
CREATE INDEX idx_identity_review_dec_reviewer ON identity_access_review_decision(reviewer_user_id);