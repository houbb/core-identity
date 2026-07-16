CREATE TABLE IF NOT EXISTS identity_access_package (
    id                      TEXT    NOT NULL,
    organization_id         TEXT    NOT NULL,
    package_code            TEXT    NOT NULL,
    name                    TEXT    NOT NULL,
    description             TEXT,
    package_type            TEXT    NOT NULL DEFAULT 'STANDARD',
    risk_level              TEXT    NOT NULL DEFAULT 'LOW',
    requestable             INTEGER NOT NULL DEFAULT 1,
    default_duration_seconds INTEGER,
    max_duration_seconds    INTEGER,
    required_auth_level     TEXT    DEFAULT 'AUTH_LEVEL_1',
    owner_user_id           TEXT,
    approval_policy_json    TEXT,
    eligibility_policy_json TEXT,
    status                  TEXT    NOT NULL DEFAULT 'ACTIVE',
    created_at              INTEGER NOT NULL,
    updated_at              INTEGER NOT NULL,
    version                 INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_identity_access_pkg_code ON identity_access_package(organization_id, package_code);
CREATE INDEX idx_identity_access_pkg_org ON identity_access_package(organization_id);
CREATE INDEX idx_identity_access_pkg_type ON identity_access_package(package_type);
CREATE INDEX idx_identity_access_pkg_status ON identity_access_package(status);
CREATE INDEX idx_identity_access_pkg_risk ON identity_access_package(risk_level);
