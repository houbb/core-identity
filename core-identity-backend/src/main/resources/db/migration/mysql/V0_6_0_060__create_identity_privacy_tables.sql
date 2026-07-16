CREATE TABLE IF NOT EXISTS identity_privacy_request (
    id                VARCHAR(36)   NOT NULL,
    user_id           VARCHAR(36)   NOT NULL,
    organization_id   VARCHAR(36),
    request_type      VARCHAR(30)   NOT NULL,
    jurisdiction      VARCHAR(30),
    status            VARCHAR(30)   NOT NULL DEFAULT 'SUBMITTED',
    verification_level VARCHAR(30),
    submitted_at      BIGINT        NOT NULL,
    due_at            BIGINT,
    completed_at      BIGINT,
    rejection_reason  VARCHAR(1000),
    created_at        BIGINT        NOT NULL,
    updated_at        BIGINT        NOT NULL,
    version           BIGINT        NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_privacy_req_user ON identity_privacy_request(user_id);
CREATE INDEX idx_identity_privacy_req_status ON identity_privacy_request(status);
CREATE INDEX idx_identity_privacy_req_type ON identity_privacy_request(request_type);

CREATE TABLE IF NOT EXISTS identity_privacy_request_task (
    id                  VARCHAR(36)  NOT NULL,
    privacy_request_id  VARCHAR(36)  NOT NULL,
    target_service      VARCHAR(60)  NOT NULL,
    task_type           VARCHAR(30)  NOT NULL,
    status              VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    result_summary      TEXT,
    retention_reason    VARCHAR(500),
    evidence_reference  VARCHAR(255),
    started_at          BIGINT,
    completed_at        BIGINT,
    updated_at          BIGINT       NOT NULL,
    version             BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_privacy_task_req ON identity_privacy_request_task(privacy_request_id);
CREATE INDEX idx_identity_privacy_task_service ON identity_privacy_request_task(target_service);
CREATE INDEX idx_identity_privacy_task_status ON identity_privacy_request_task(status);

CREATE TABLE IF NOT EXISTS identity_legal_hold (
    id               VARCHAR(36)   NOT NULL,
    organization_id  VARCHAR(36),
    case_reference   VARCHAR(120)  NOT NULL,
    name             VARCHAR(200)  NOT NULL,
    reason           VARCHAR(1000),
    status           VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
    approved_by      VARCHAR(36),
    effective_at     BIGINT        NOT NULL,
    review_at        BIGINT,
    released_at      BIGINT,
    created_by       VARCHAR(36),
    created_at       BIGINT        NOT NULL,
    updated_at       BIGINT        NOT NULL,
    version          BIGINT        NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_legal_hold_org ON identity_legal_hold(organization_id);
CREATE INDEX idx_identity_legal_hold_status ON identity_legal_hold(status);

CREATE TABLE IF NOT EXISTS identity_legal_hold_scope (
    id               VARCHAR(36) NOT NULL,
    legal_hold_id    VARCHAR(36) NOT NULL,
    scope_type       VARCHAR(30) NOT NULL,
    scope_reference  VARCHAR(255),
    data_category    VARCHAR(60),
    period_start     BIGINT,
    period_end       BIGINT,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_legal_hold_scope_hold ON identity_legal_hold_scope(legal_hold_id);

CREATE TABLE IF NOT EXISTS identity_retention_policy (
    id                VARCHAR(36)  NOT NULL,
    organization_id   VARCHAR(36),
    data_category     VARCHAR(60)  NOT NULL,
    trigger_type      VARCHAR(30)  NOT NULL,
    retention_seconds BIGINT       NOT NULL,
    expiration_action VARCHAR(30)  NOT NULL DEFAULT 'DELETE',
    jurisdiction      VARCHAR(30),
    priority          INTEGER      NOT NULL DEFAULT 50,
    status            VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    owner_user_id     VARCHAR(36),
    created_at        BIGINT       NOT NULL,
    updated_at        BIGINT       NOT NULL,
    version           BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_retention_pol_org ON identity_retention_policy(organization_id);
CREATE INDEX idx_identity_retention_pol_cat ON identity_retention_policy(data_category);
CREATE INDEX idx_identity_retention_pol_status ON identity_retention_policy(status);

CREATE TABLE IF NOT EXISTS identity_processing_activity (
    id                        VARCHAR(36)  NOT NULL,
    organization_id           VARCHAR(36),
    activity_code             VARCHAR(60)  NOT NULL,
    name                      VARCHAR(200) NOT NULL,
    purpose                   VARCHAR(1000),
    controller                VARCHAR(200),
    processor                 VARCHAR(200),
    data_subject_categories_json TEXT,
    data_categories_json      TEXT,
    recipient_categories_json TEXT,
    transfer_details_json     TEXT,
    retention_summary         VARCHAR(500),
    security_measures_summary VARCHAR(500),
    owner_user_id             VARCHAR(36),
    status                    VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at                BIGINT       NOT NULL,
    updated_at                BIGINT       NOT NULL,
    version                   BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX uq_identity_processing_act_code ON identity_processing_activity(activity_code);
CREATE INDEX idx_identity_processing_act_org ON identity_processing_activity(organization_id);