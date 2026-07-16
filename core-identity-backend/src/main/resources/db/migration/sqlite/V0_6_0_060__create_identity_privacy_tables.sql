CREATE TABLE IF NOT EXISTS identity_privacy_request (
    id                 TEXT    NOT NULL,
    user_id            TEXT    NOT NULL,
    organization_id    TEXT,
    request_type       TEXT    NOT NULL,
    jurisdiction       TEXT,
    status             TEXT    NOT NULL DEFAULT 'SUBMITTED',
    verification_level TEXT,
    submitted_at       INTEGER NOT NULL,
    due_at             INTEGER,
    completed_at       INTEGER,
    rejection_reason   TEXT,
    created_at         INTEGER NOT NULL,
    updated_at         INTEGER NOT NULL,
    version            INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_privacy_req_user ON identity_privacy_request(user_id);
CREATE INDEX idx_identity_privacy_req_status ON identity_privacy_request(status);
CREATE INDEX idx_identity_privacy_req_type ON identity_privacy_request(request_type);

CREATE TABLE IF NOT EXISTS identity_privacy_request_task (
    id                  TEXT    NOT NULL,
    privacy_request_id  TEXT    NOT NULL,
    target_service      TEXT    NOT NULL,
    task_type           TEXT    NOT NULL,
    status              TEXT    NOT NULL DEFAULT 'PENDING',
    result_summary      TEXT,
    retention_reason    TEXT,
    evidence_reference  TEXT,
    started_at          INTEGER,
    completed_at        INTEGER,
    updated_at          INTEGER NOT NULL,
    version             INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_privacy_task_req ON identity_privacy_request_task(privacy_request_id);
CREATE INDEX idx_identity_privacy_task_service ON identity_privacy_request_task(target_service);
CREATE INDEX idx_identity_privacy_task_status ON identity_privacy_request_task(status);

CREATE TABLE IF NOT EXISTS identity_legal_hold (
    id               TEXT    NOT NULL,
    organization_id  TEXT,
    case_reference   TEXT    NOT NULL,
    name             TEXT    NOT NULL,
    reason           TEXT,
    status           TEXT    NOT NULL DEFAULT 'ACTIVE',
    approved_by      TEXT,
    effective_at     INTEGER NOT NULL,
    review_at        INTEGER,
    released_at      INTEGER,
    created_by       TEXT,
    created_at       INTEGER NOT NULL,
    updated_at       INTEGER NOT NULL,
    version          INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_legal_hold_org ON identity_legal_hold(organization_id);
CREATE INDEX idx_identity_legal_hold_status ON identity_legal_hold(status);

CREATE TABLE IF NOT EXISTS identity_legal_hold_scope (
    id               TEXT NOT NULL,
    legal_hold_id    TEXT NOT NULL,
    scope_type       TEXT NOT NULL,
    scope_reference  TEXT,
    data_category    TEXT,
    period_start     INTEGER,
    period_end       INTEGER,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_legal_hold_scope_hold ON identity_legal_hold_scope(legal_hold_id);

CREATE TABLE IF NOT EXISTS identity_retention_policy (
    id                TEXT    NOT NULL,
    organization_id   TEXT,
    data_category     TEXT    NOT NULL,
    trigger_type      TEXT    NOT NULL,
    retention_seconds INTEGER NOT NULL,
    expiration_action TEXT    NOT NULL DEFAULT 'DELETE',
    jurisdiction      TEXT,
    priority          INTEGER NOT NULL DEFAULT 50,
    status            TEXT    NOT NULL DEFAULT 'ACTIVE',
    owner_user_id     TEXT,
    created_at        INTEGER NOT NULL,
    updated_at        INTEGER NOT NULL,
    version           INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_retention_pol_org ON identity_retention_policy(organization_id);
CREATE INDEX idx_identity_retention_pol_cat ON identity_retention_policy(data_category);
CREATE INDEX idx_identity_retention_pol_status ON identity_retention_policy(status);

CREATE TABLE IF NOT EXISTS identity_processing_activity (
    id                          TEXT    NOT NULL,
    organization_id             TEXT,
    activity_code               TEXT    NOT NULL,
    name                        TEXT    NOT NULL,
    purpose                     TEXT,
    controller                  TEXT,
    processor                   TEXT,
    data_subject_categories_json TEXT,
    data_categories_json        TEXT,
    recipient_categories_json   TEXT,
    transfer_details_json       TEXT,
    retention_summary           TEXT,
    security_measures_summary   TEXT,
    owner_user_id               TEXT,
    status                      TEXT    NOT NULL DEFAULT 'ACTIVE',
    created_at                  INTEGER NOT NULL,
    updated_at                  INTEGER NOT NULL,
    version                     INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_identity_processing_act_code ON identity_processing_activity(activity_code);
CREATE INDEX idx_identity_processing_act_org ON identity_processing_activity(organization_id);