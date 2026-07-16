CREATE TABLE IF NOT EXISTS identity_compliance_control (
    id              VARCHAR(36)   NOT NULL,
    control_code    VARCHAR(60)   NOT NULL,
    name            VARCHAR(200)  NOT NULL,
    description     VARCHAR(2000),
    control_type    VARCHAR(30)   NOT NULL DEFAULT 'DETECTIVE',
    owner_user_id   VARCHAR(36),
    frequency       VARCHAR(30)   DEFAULT 'MONTHLY',
    status          VARCHAR(20)   NOT NULL DEFAULT 'PLANNED',
    created_at      BIGINT        NOT NULL,
    updated_at      BIGINT        NOT NULL,
    version         BIGINT        NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX uq_identity_ctrl_code ON identity_compliance_control(control_code);
CREATE INDEX idx_identity_ctrl_status ON identity_compliance_control(status);
CREATE INDEX idx_identity_ctrl_owner ON identity_compliance_control(owner_user_id);

CREATE TABLE IF NOT EXISTS identity_compliance_framework (
    id              VARCHAR(36)  NOT NULL,
    framework_code  VARCHAR(60)  NOT NULL,
    name            VARCHAR(200) NOT NULL,
    version         VARCHAR(30),
    publisher       VARCHAR(150),
    status          VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at      BIGINT       NOT NULL,
    updated_at      BIGINT       NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX uq_identity_framework_code ON identity_compliance_framework(framework_code);

CREATE TABLE IF NOT EXISTS identity_control_mapping (
    control_id      VARCHAR(36) NOT NULL,
    framework_id    VARCHAR(36) NOT NULL,
    requirement_code VARCHAR(60) NOT NULL,
    mapping_type    VARCHAR(30) DEFAULT 'DIRECT',
    notes           VARCHAR(500),
    PRIMARY KEY (control_id, framework_id, requirement_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_ctrl_map_ctrl ON identity_control_mapping(control_id);
CREATE INDEX idx_identity_ctrl_map_fw ON identity_control_mapping(framework_id);

CREATE TABLE IF NOT EXISTS identity_control_assessment (
    id              VARCHAR(36)  NOT NULL,
    control_id      VARCHAR(36)  NOT NULL,
    assessed_by     VARCHAR(36),
    assessment_date BIGINT       NOT NULL,
    result          VARCHAR(30)  NOT NULL DEFAULT 'NOT_TESTED',
    findings_summary TEXT,
    evidence_refs   TEXT,
    created_at      BIGINT       NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_ctrl_asmnt_ctrl ON identity_control_assessment(control_id);

CREATE TABLE IF NOT EXISTS identity_control_finding (
    id              VARCHAR(36)   NOT NULL,
    control_id      VARCHAR(36)   NOT NULL,
    assessment_id   VARCHAR(36),
    title           VARCHAR(200)  NOT NULL,
    description     VARCHAR(2000),
    severity        VARCHAR(20)   NOT NULL DEFAULT 'MEDIUM',
    status          VARCHAR(30)   NOT NULL DEFAULT 'OPEN',
    owner_user_id   VARCHAR(36),
    due_at          BIGINT,
    resolved_at     BIGINT,
    evidence_refs   TEXT,
    created_at      BIGINT        NOT NULL,
    updated_at      BIGINT        NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_finding_ctrl ON identity_control_finding(control_id);
CREATE INDEX idx_identity_finding_status ON identity_control_finding(status);
CREATE INDEX idx_identity_finding_severity ON identity_control_finding(severity);

CREATE TABLE IF NOT EXISTS identity_evidence (
    id                VARCHAR(36)  NOT NULL,
    control_id        VARCHAR(36)  NOT NULL,
    evidence_type     VARCHAR(30)  NOT NULL,
    source_service    VARCHAR(60),
    source_reference  VARCHAR(255),
    period_start      BIGINT,
    period_end        BIGINT,
    content_location  VARCHAR(500),
    checksum          VARCHAR(128),
    signature         TEXT,
    collected_at      BIGINT       NOT NULL,
    collected_by      VARCHAR(36),
    status            VARCHAR(20)  NOT NULL DEFAULT 'VALID',
    created_at        BIGINT       NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_evidence_ctrl ON identity_evidence(control_id);
CREATE INDEX idx_identity_evidence_status ON identity_evidence(status);