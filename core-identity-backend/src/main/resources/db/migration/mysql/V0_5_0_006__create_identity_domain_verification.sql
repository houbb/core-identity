CREATE TABLE IF NOT EXISTS identity_domain_verification (
    id                   VARCHAR(36)  NOT NULL,
    domain_id            VARCHAR(36)  NOT NULL,
    challenge_hash       VARCHAR(255),
    expected_record_name VARCHAR(255),
    method               VARCHAR(30)  NOT NULL DEFAULT 'DNS_TXT',
    status               VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    attempt_count        INTEGER      NOT NULL DEFAULT 0,
    expires_at           BIGINT,
    verified_at          BIGINT,
    created_at           BIGINT       NOT NULL,
    updated_at           BIGINT       NOT NULL,
    version              BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_dv_domain ON identity_domain_verification(domain_id);
