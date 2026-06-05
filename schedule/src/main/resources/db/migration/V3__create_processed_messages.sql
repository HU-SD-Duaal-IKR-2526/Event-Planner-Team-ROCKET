-- V3: Verwerkte berichten (idempotentie deduplicatie)

CREATE TABLE processed_messages (
    message_id   VARCHAR(100) NOT NULL,
    processed_at TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT pk_processed_messages PRIMARY KEY (message_id)
);

