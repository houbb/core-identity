CREATE TABLE IF NOT EXISTS identity_access_request (
    id                  TEXT    NOT NULL,
    requester_user_id   TEXT    NOT NULL,
    target_subject_type TEXT    NOT NULL DEFAULT 'USER',
    target_subject_id   TEXT    NOT NULL,
    organization_id     TEXT    NOT NULL,
    access_package_id   TEXT    NOT NULL,
    business_reason     TEXT,
    ticket_reference    TEXT,
    requested_start_at  INTEGER,
    requested_end_at    INTEGER,
    status              TEXT    NOT NULL DEFAULT 'DRAFT',
    risk_level          TEXT    NOT NULL DEFAULT 'LOW',
    sod_result          TEXT,
    submitted_at        INTEGER,
    completed_at        INTEGER,
    created_at          INTEGER NOT NULL,
    updated_at          INTEGER NOT NULL,
    version             INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_access_req_requester ON identity_access_request(requester_user_id);
CREATE INDEX idx_identity_access_req_org ON identity_access_request(organization_id);
CREATE INDEX idx_identity_access_req_package ON identity_access_request(access_package_id);
CREATE INDEX idx_identity_access_req_status ON identity_access_request(status);
CREATE INDEX idx_identity_access_req_submitted ON identity_access_request(submitted_at);