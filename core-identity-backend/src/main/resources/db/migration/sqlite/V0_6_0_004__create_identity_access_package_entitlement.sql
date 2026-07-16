CREATE TABLE IF NOT EXISTS identity_access_package_entitlement (
    package_id      TEXT NOT NULL,
    entitlement_id  TEXT NOT NULL,
    created_at      INTEGER NOT NULL,
    PRIMARY KEY (package_id, entitlement_id)
);

CREATE INDEX idx_identity_access_pkg_ent_pkg ON identity_access_package_entitlement(package_id);
CREATE INDEX idx_identity_access_pkg_ent_ent ON identity_access_package_entitlement(entitlement_id);
