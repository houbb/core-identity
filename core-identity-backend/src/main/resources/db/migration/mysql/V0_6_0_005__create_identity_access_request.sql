CREATE TABLE IF NOT EXISTS identity_access_request (
    id                  VARCHAR(36)   NOT NULL,
    requester_user_id   VARCHAR(36)   NOT NULL,
    target_subject_type VARCHAR(30)   NOT NULL DEFAULT 'USER',
    target_subject_id   VARCHAR(36)   NOT NULL,
    organization_id     VARCHAR(36)   NOT NULL,
    access_package_id   VARCHAR(36)   NOT NULL,
    business_reason     VARCHAR(2000),
    ticket_reference    VARCHAR(255),
    requested_start_at  BIGINT,
    requested_end_at    BIGINT,
    status              VARCHAR(30)   NOT NULL DEFAULT 'DRAFT',
    risk_level          VARCHAR(20)   NOT NULL DEFAULT 'LOW',
    sod_result          VARCHAR(30),
    submitted_at        BIGINT,
    completed_at        BIGINT,
    created_at          BIGINT        NOT NULL,
    updated_at          BIGINT        NOT NULL,
    version             BIGINT        NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_access_req_requester ON identity_access_request(requester_user_id);
CREATE INDEX idx_identity_access_req_org ON identity_access_request(organization_id);
CREATE INDEX idx_identity_access_req_package ON identity_access_request(access_package_id);
CREATE INDEX idx_identity_access_req_status ON identity_access_request(status);
CREATE INDEX idx_identity_access_req_submitted ON identity_access_request(submitted_at);
