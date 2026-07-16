CREATE TABLE IF NOT EXISTS identity_access_package (
    id                       VARCHAR(36)   NOT NULL,
    organization_id          VARCHAR(36)   NOT NULL,
    package_code             VARCHAR(120)  NOT NULL,
    name                     VARCHAR(150)  NOT NULL,
    description              VARCHAR(1000),
    package_type             VARCHAR(30)   NOT NULL DEFAULT 'STANDARD',
    risk_level               VARCHAR(20)   NOT NULL DEFAULT 'LOW',
    requestable              INTEGER       NOT NULL DEFAULT 1,
    default_duration_seconds BIGINT,
    max_duration_seconds     BIGINT,
    required_auth_level      VARCHAR(30)   DEFAULT 'AUTH_LEVEL_1',
    owner_user_id            VARCHAR(36),
    approval_policy_json     TEXT,
    eligibility_policy_json  TEXT,
    status                   VARCHAR(30)   NOT NULL DEFAULT 'ACTIVE',
    created_at               BIGINT        NOT NULL,
    updated_at               BIGINT        NOT NULL,
    version                  BIGINT        NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX uq_identity_access_pkg_code ON identity_access_package(organization_id, package_code);
CREATE INDEX idx_identity_access_pkg_org ON identity_access_package(organization_id);
CREATE INDEX idx_identity_access_pkg_type ON identity_access_package(package_type);
CREATE INDEX idx_identity_access_pkg_status ON identity_access_package(status);
CREATE INDEX idx_identity_access_pkg_risk ON identity_access_package(risk_level);
