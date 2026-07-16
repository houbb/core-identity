CREATE TABLE IF NOT EXISTS identity_access_review_item (
    id               VARCHAR(36) NOT NULL,
    campaign_id      VARCHAR(36) NOT NULL,
    subject_type     VARCHAR(30) NOT NULL,
    subject_id       VARCHAR(36) NOT NULL,
    entitlement_id   VARCHAR(36) NOT NULL,
    grant_id         VARCHAR(36),
    reviewer_user_id VARCHAR(36),
    risk_level       VARCHAR(20) NOT NULL DEFAULT 'LOW',
    last_used_at     BIGINT,
    status           VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at       BIGINT      NOT NULL,
    updated_at       BIGINT      NOT NULL,
    version          BIGINT      NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_review_item_camp ON identity_access_review_item(campaign_id);
CREATE INDEX idx_identity_review_item_subject ON identity_access_review_item(subject_id);
CREATE INDEX idx_identity_review_item_reviewer ON identity_access_review_item(reviewer_user_id);
CREATE INDEX idx_identity_review_item_status ON identity_access_review_item(status);