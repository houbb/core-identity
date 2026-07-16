-- V0.7.0.010: Create identity_event_subscription table (P7.3)
CREATE TABLE IF NOT EXISTS identity_event_subscription (
    id                       VARCHAR(36)   NOT NULL PRIMARY KEY,
    subscriber_type          VARCHAR(30)   NOT NULL DEFAULT 'WEBHOOK',
    subscriber_id            VARCHAR(36)   NOT NULL,
    event_pattern            VARCHAR(200)  NOT NULL,
    endpoint                 VARCHAR(1000) NOT NULL,
    signing_secret_reference VARCHAR(500)  NULL,
    status                   VARCHAR(30)   NOT NULL DEFAULT 'ACTIVE',
    retry_policy_json        TEXT          NULL,
    created_at               BIGINT        NOT NULL,
    updated_at               BIGINT        NOT NULL,
    version                  BIGINT        NOT NULL DEFAULT 1
);

CREATE INDEX idx_event_sub_status ON identity_event_subscription(status);