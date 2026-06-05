-- V2: Transactional Outbox

CREATE TABLE outbox_messages (
    id          UUID         NOT NULL,
    routing_key VARCHAR(100) NOT NULL,
    payload     TEXT         NOT NULL,
    status      VARCHAR(10)  NOT NULL DEFAULT 'PENDING',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    published_at TIMESTAMPTZ,
    CONSTRAINT pk_outbox PRIMARY KEY (id)
);

-- Index voor het ophalen van PENDING berichten (efficient poll)
CREATE INDEX idx_outbox_pending
    ON outbox_messages (status, created_at)
    WHERE status = 'PENDING';

