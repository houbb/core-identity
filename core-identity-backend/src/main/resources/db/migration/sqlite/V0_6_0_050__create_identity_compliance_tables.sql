CREATE TABLE IF NOT EXISTS identity_compliance_control (
    id              TEXT    NOT NULL,
    control_code    TEXT    NOT NULL,
    name            TEXT    NOT NULL,
    description     TEXT,
    control_type    TEXT    NOT NULL DEFAULT 'DETECTIVE',
    owner_user_id   TEXT,
    frequency       TEXT    DEFAULT 'MONTHLY',
    status          TEXT    NOT NULL DEFAULT 'PLANNED',
    created_at      INTEGER NOT NULL,
    updated_at      INTEGER NOT NULL,
    version         INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_identity_ctrl_code ON identity_compliance_control(control_code);
CREATE INDEX idx_identity_ctrl_status ON identity_compliance_control(status);
CREATE INDEX idx_identity_ctrl_owner ON identity_compliance_control(owner_user_id);

CREATE TABLE IF NOT EXISTS identity_compliance_framework (
    id             TEXT    NOT NULL,
    framework_code TEXT    NOT NULL,
    name           TEXT    NOT NULL,
    version        TEXT,
    publisher      TEXT,
    status         TEXT    NOT NULL DEFAULT 'ACTIVE',
    created_at     INTEGER NOT NULL,
    updated_at     INTEGER NOT NULL,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_identity_framework_code ON identity_compliance_framework(framework_code);

CREATE TABLE IF NOT EXISTS identity_control_mapping (
    control_id       TEXT NOT NULL,
    framework_id     TEXT NOT NULL,
    requirement_code TEXT NOT NULL,
    mapping_type     TEXT DEFAULT 'DIRECT',
    notes            TEXT,
    PRIMARY KEY (control_id, framework_id, requirement_code)
);

CREATE INDEX idx_identity_ctrl_map_ctrl ON identity_control_mapping(control_id);
CREATE INDEX idx_identity_ctrl_map_fw ON identity_control_mapping(framework_id);

CREATE TABLE IF NOT EXISTS identity_control_assessment (
    id               TEXT    NOT NULL,
    control_id       TEXT    NOT NULL,
    assessed_by      TEXT,
    assessment_date  INTEGER NOT NULL,
    result           TEXT    NOT NULL DEFAULT 'NOT_TESTED',
    findings_summary TEXT,
    evidence_refs    TEXT,
    created_at       INTEGER NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_ctrl_asmnt_ctrl ON identity_control_assessment(control_id);

CREATE TABLE IF NOT EXISTS identity_control_finding (
    id            TEXT    NOT NULL,
    control_id    TEXT    NOT NULL,
    assessment_id TEXT,
    title         TEXT    NOT NULL,
    description   TEXT,
    severity      TEXT    NOT NULL DEFAULT 'MEDIUM',
    status        TEXT    NOT NULL DEFAULT 'OPEN',
    owner_user_id TEXT,
    due_at        INTEGER,
    resolved_at   INTEGER,
    evidence_refs TEXT,
    created_at    INTEGER NOT NULL,
    updated_at    INTEGER NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_finding_ctrl ON identity_control_finding(control_id);
CREATE INDEX idx_identity_finding_status ON identity_control_finding(status);
CREATE INDEX idx_identity_finding_severity ON identity_control_finding(severity);

CREATE TABLE IF NOT EXISTS identity_evidence (
    id               TEXT    NOT NULL,
    control_id       TEXT    NOT NULL,
    evidence_type    TEXT    NOT NULL,
    source_service   TEXT,
    source_reference TEXT,
    period_start     INTEGER,
    period_end       INTEGER,
    content_location TEXT,
    checksum         TEXT,
    signature        TEXT,
    collected_at     INTEGER NOT NULL,
    collected_by     TEXT,
    status           TEXT    NOT NULL DEFAULT 'VALID',
    created_at       INTEGER NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_evidence_ctrl ON identity_evidence(control_id);
CREATE INDEX idx_identity_evidence_status ON identity_evidence(status);