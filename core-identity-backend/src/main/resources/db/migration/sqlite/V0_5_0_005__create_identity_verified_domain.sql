CREATE TABLE IF NOT EXISTS identity_verified_domain (
    id                  TEXT    NOT NULL,
    organization_id     TEXT    NOT NULL,
    domain_name         TEXT    NOT NULL,
    status              TEXT    NOT NULL DEFAULT 'PENDING',
    verification_method TEXT,
    verified_at         INTEGER,
    last_checked_at     INTEGER,
    expires_at          INTEGER,
    conflict_reason     TEXT,
    created_by          TEXT,
    created_at          INTEGER NOT NULL,
    updated_at          INTEGER NOT NULL,
    version             INTEGER NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE UNIQUE INDEX uq_identity_vd_domain ON identity_verified_domain(domain_name);
CREATE INDEX idx_identity_vd_org ON identity_verified_domain(organization_id);
