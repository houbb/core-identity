CREATE TABLE IF NOT EXISTS identity_scim_group_member (
    group_id             TEXT    NOT NULL,
    external_identity_id TEXT    NOT NULL,
    membership_id        TEXT,
    created_at           INTEGER NOT NULL,
    PRIMARY KEY (group_id, external_identity_id)
);

CREATE INDEX idx_identity_sgm_ei ON identity_scim_group_member(external_identity_id);
CREATE INDEX idx_identity_sgm_mem ON identity_scim_group_member(membership_id);
