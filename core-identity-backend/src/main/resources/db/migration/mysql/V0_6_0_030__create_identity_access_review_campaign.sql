CREATE TABLE IF NOT EXISTS identity_access_review_campaign (
    id                   VARCHAR(36)   NOT NULL,
    organization_id      VARCHAR(36),
    name                 VARCHAR(200)  NOT NULL,
    campaign_type        VARCHAR(30)   NOT NULL,
    scope_json           TEXT,
    reviewer_policy_json TEXT,
    status               VARCHAR(30)   NOT NULL DEFAULT 'DRAFT',
    starts_at            BIGINT,
    due_at               BIGINT,
    completed_at         BIGINT,
    created_by           VARCHAR(36),
    created_at           BIGINT        NOT NULL,
    updated_at           BIGINT        NOT NULL,
    version              BIGINT        NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_review_camp_org ON identity_access_review_campaign(organization_id);
CREATE INDEX idx_identity_review_camp_status ON identity_access_review_campaign(status);
CREATE INDEX idx_identity_review_camp_due ON identity_access_review_campaign(due_at);