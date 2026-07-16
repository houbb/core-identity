CREATE TABLE IF NOT EXISTS identity_scim_group_member (
    group_id             VARCHAR(36) NOT NULL,
    external_identity_id VARCHAR(36) NOT NULL,
    membership_id        VARCHAR(36),
    created_at           BIGINT      NOT NULL,
    PRIMARY KEY (group_id, external_identity_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_identity_sgm_ei ON identity_scim_group_member(external_identity_id);
CREATE INDEX idx_identity_sgm_mem ON identity_scim_group_member(membership_id);
