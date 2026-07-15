-- V0.4.0.160: Create identity_security_policy table
CREATE TABLE IF NOT EXISTS identity_security_policy (
    id                               VARCHAR(36)  NOT NULL,
    organization_id                  VARCHAR(36)  NOT NULL,
    name                             VARCHAR(150) NOT NULL,
    status                           VARCHAR(30)  NOT NULL DEFAULT 'DRAFT',
    minimum_auth_level               VARCHAR(30),
    phishing_resistant_required      INTEGER      NOT NULL DEFAULT 0,
    allowed_authenticator_types_json TEXT,
    privileged_roles_only            INTEGER      NOT NULL DEFAULT 0,
    trusted_device_days              INTEGER,
    session_idle_seconds             INTEGER,
    session_absolute_seconds         INTEGER,
    reauth_seconds                   INTEGER,
    grace_period_ends_at             BIGINT,
    created_by                       VARCHAR(36),
    published_at                     BIGINT,
    created_at                       BIGINT       NOT NULL,
    updated_at                       BIGINT       NOT NULL,
    version                          BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_policy_org ON identity_security_policy(organization_id, status);