CREATE TABLE IF NOT EXISTS identity_access_review_campaign (
    id                   TEXT    NOT NULL,
    organization_id      TEXT,
    name                 TEXT    NOT NULL,
    campaign_type        TEXT    NOT NULL,
    scope_json           TEXT,
    reviewer_policy_json TEXT,
    status               TEXT    NOT NULL DEFAULT 'DRAFT',
    starts_at            INTEGER,
    due_at               INTEGER,
    completed_at         INTEGER,
    created_by           TEXT,
    created_at           INTEGER NOT NULL,
    updated_at           INTEGER NOT NULL,
    version              INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_review_camp_org ON identity_access_review_campaign(organization_id);
CREATE INDEX idx_identity_review_camp_status ON identity_access_review_campaign(status);
CREATE INDEX idx_identity_review_camp_due ON identity_access_review_campaign(due_at);