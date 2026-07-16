CREATE TABLE IF NOT EXISTS identity_verified_domain (
    id                  VARCHAR(36)  NOT NULL,
    organization_id     VARCHAR(36)  NOT NULL,
    domain_name         VARCHAR(255) NOT NULL,
    status              VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    verification_method VARCHAR(30),
    verified_at         BIGINT,
    last_checked_at     BIGINT,
    expires_at          BIGINT,
    conflict_reason     VARCHAR(500),
    created_by          VARCHAR(36),
    created_at          BIGINT       NOT NULL,
    updated_at          BIGINT       NOT NULL,
    version             BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE UNIQUE INDEX uq_identity_vd_domain ON identity_verified_domain(domain_name);
CREATE INDEX idx_identity_vd_org ON identity_verified_domain(organization_id);
