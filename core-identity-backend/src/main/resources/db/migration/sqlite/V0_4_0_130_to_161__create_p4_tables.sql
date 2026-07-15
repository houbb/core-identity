-- V0.4.0.130: Create identity_device table (SQLite)
CREATE TABLE IF NOT EXISTS identity_device (
    id                 TEXT    NOT NULL,
    user_id            TEXT    NOT NULL,
    device_cookie_hash TEXT,
    display_name       TEXT,
    browser            TEXT,
    operating_system   TEXT,
    first_seen_at      INTEGER NOT NULL,
    last_seen_at       INTEGER NOT NULL,
    last_ip            TEXT,
    status             TEXT    NOT NULL DEFAULT 'ACTIVE',
    created_at         INTEGER NOT NULL,
    updated_at         INTEGER NOT NULL,
    version            INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_device_user ON identity_device(user_id);
CREATE INDEX idx_identity_device_cookie ON identity_device(device_cookie_hash);
-- V0.4.0.140: Create identity_risk_assessment table (SQLite)
CREATE TABLE IF NOT EXISTS identity_risk_assessment (
    id                  TEXT    NOT NULL,
    user_id             TEXT,
    session_id          TEXT,
    operation           TEXT,
    risk_level          TEXT    NOT NULL,
    decision            TEXT    NOT NULL,
    required_auth_level TEXT,
    score               INTEGER NOT NULL DEFAULT 0,
    reasons_json        TEXT,
    model_version       TEXT,
    request_id          TEXT,
    created_at          INTEGER NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_risk_user ON identity_risk_assessment(user_id);
CREATE INDEX idx_identity_risk_session ON identity_risk_assessment(session_id);
CREATE INDEX idx_identity_risk_created ON identity_risk_assessment(created_at);
-- V0.4.0.141: Create identity_risk_signal table (SQLite)
CREATE TABLE IF NOT EXISTS identity_risk_signal (
    id                TEXT    NOT NULL,
    assessment_id     TEXT    NOT NULL,
    signal_type       TEXT    NOT NULL,
    signal_value_hash TEXT,
    weight            INTEGER NOT NULL DEFAULT 0,
    result            TEXT,
    metadata_json     TEXT,
    created_at        INTEGER NOT NULL,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_risk_signal_assessment ON identity_risk_signal(assessment_id);
-- V0.4.0.142: Create identity_security_event table (SQLite)
CREATE TABLE IF NOT EXISTS identity_security_event (
    id                 TEXT    NOT NULL,
    user_id            TEXT,
    organization_id    TEXT,
    event_type         TEXT    NOT NULL,
    severity           TEXT    NOT NULL DEFAULT 'INFO',
    status             TEXT    NOT NULL DEFAULT 'OPEN',
    source             TEXT,
    risk_assessment_id TEXT,
    title              TEXT,
    description        TEXT,
    metadata_json      TEXT,
    detected_at        INTEGER NOT NULL,
    resolved_at        INTEGER,
    resolved_by        TEXT,
    resolution         TEXT,
    created_at         INTEGER NOT NULL,
    updated_at         INTEGER NOT NULL,
    version            INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_security_event_user ON identity_security_event(user_id, status);
CREATE INDEX idx_identity_security_event_org ON identity_security_event(organization_id);
CREATE INDEX idx_identity_security_event_type ON identity_security_event(event_type);
CREATE INDEX idx_identity_security_event_detected ON identity_security_event(detected_at);
-- V0.4.0.143: Create identity_login_throttle table (SQLite)
CREATE TABLE IF NOT EXISTS identity_login_throttle (
    id                TEXT    NOT NULL,
    dimension_type    TEXT    NOT NULL,
    dimension_hash    TEXT    NOT NULL,
    failure_count     INTEGER NOT NULL DEFAULT 0,
    blocked_until     INTEGER,
    last_failure_at   INTEGER,
    window_started_at INTEGER,
    updated_at        INTEGER NOT NULL,
    version           INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_identity_throttle_dim ON identity_login_throttle(dimension_type, dimension_hash);
-- V0.4.0.150: Create identity_account_recovery table (SQLite)
CREATE TABLE IF NOT EXISTS identity_account_recovery (
    id                      TEXT    NOT NULL,
    user_id                 TEXT    NOT NULL,
    recovery_type           TEXT    NOT NULL,
    status                  TEXT    NOT NULL DEFAULT 'PENDING_VERIFICATION',
    risk_level              TEXT,
    required_evidence_level TEXT,
    initiated_ip            TEXT,
    initiated_device_id     TEXT,
    cooling_off_until       INTEGER,
    approved_by             TEXT,
    rejected_by             TEXT,
    completed_at            INTEGER,
    cancelled_at            INTEGER,
    created_at              INTEGER NOT NULL,
    updated_at              INTEGER NOT NULL,
    version                 INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_recovery_user ON identity_account_recovery(user_id, status);
CREATE INDEX idx_identity_recovery_status ON identity_account_recovery(status);
-- V0.4.0.160: Create identity_security_policy table (SQLite)
CREATE TABLE IF NOT EXISTS identity_security_policy (
    id                               TEXT    NOT NULL,
    organization_id                  TEXT    NOT NULL,
    name                             TEXT    NOT NULL,
    status                           TEXT    NOT NULL DEFAULT 'DRAFT',
    minimum_auth_level               TEXT,
    phishing_resistant_required      INTEGER NOT NULL DEFAULT 0,
    allowed_authenticator_types_json TEXT,
    privileged_roles_only            INTEGER NOT NULL DEFAULT 0,
    trusted_device_days              INTEGER,
    session_idle_seconds             INTEGER,
    session_absolute_seconds         INTEGER,
    reauth_seconds                   INTEGER,
    grace_period_ends_at             INTEGER,
    created_by                       TEXT,
    published_at                     INTEGER,
    created_at                       INTEGER NOT NULL,
    updated_at                       INTEGER NOT NULL,
    version                          INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_policy_org ON identity_security_policy(organization_id, status);
-- V0.4.0.161: Create identity_security_exemption table (SQLite)
CREATE TABLE IF NOT EXISTS identity_security_exemption (
    id            TEXT    NOT NULL,
    policy_id     TEXT    NOT NULL,
    membership_id TEXT    NOT NULL,
    reason        TEXT,
    status        TEXT    NOT NULL DEFAULT 'ACTIVE',
    expires_at    INTEGER NOT NULL,
    granted_by    TEXT,
    created_at    INTEGER NOT NULL,
    revoked_at    INTEGER,
    PRIMARY KEY (id)
);

CREATE INDEX idx_identity_exemption_policy ON identity_security_exemption(policy_id);
CREATE INDEX idx_identity_exemption_member ON identity_security_exemption(membership_id);