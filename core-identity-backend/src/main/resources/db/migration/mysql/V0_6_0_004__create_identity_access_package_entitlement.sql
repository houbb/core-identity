CREATE TABLE IF NOT EXISTS identity_access_package_entitlement (
    package_id      VARCHAR(36) NOT NULL,
    entitlement_id  VARCHAR(36) NOT NULL,
    created_at      BIGINT      NOT NULL,
    PRIMARY KEY (package_id, entitlement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_access_pkg_ent_pkg ON identity_access_package_entitlement(package_id);
CREATE INDEX idx_identity_access_pkg_ent_ent ON identity_access_package_entitlement(entitlement_id);
