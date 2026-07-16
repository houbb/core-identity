CREATE TABLE IF NOT EXISTS identity_account_link_request (
    id                  VARCHAR(36)  NOT NULL,
    connection_id       VARCHAR(36)  NOT NULL,
    external_subject    VARCHAR(500) NOT NULL,
    candidate_user_id   VARCHAR(36),
    external_email      VARCHAR(320),
    status              VARCHAR(30)  NOT NULL DEFAULT 'PENDING',
    risk_level          VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    verification_method VARCHAR(50),
    expires_at          BIGINT,
    confirmed_at        BIGINT,
    rejected_at         BIGINT,
    created_at          BIGINT       NOT NULL,
    updated_at          BIGINT       NOT NULL,
    version             BIGINT       NOT NULL DEFAULT 1,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_alr_conn ON identity_account_link_request(connection_id);
CREATE INDEX idx_identity_alr_status ON identity_account_link_request(status);
