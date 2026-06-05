-- V1: Registraties schema

CREATE TABLE event_capacity (
    event_id  UUID        NOT NULL,
    capacity  INT         NOT NULL DEFAULT 0,
    open      BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_event_capacity PRIMARY KEY (event_id)
);

CREATE TABLE registrations (
    id               UUID         NOT NULL,
    event_id         UUID         NOT NULL,
    user_id          UUID         NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    plus_ones        INT          NOT NULL DEFAULT 0,
    notes            TEXT,
    channel          VARCHAR(10)  NOT NULL DEFAULT 'WEB',
    idempotency_key  VARCHAR(36)  NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version          BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_registrations PRIMARY KEY (id),
    CONSTRAINT uq_idempotency_key UNIQUE (idempotency_key)
);

-- Max 1 actieve registratie per (event, user)
CREATE UNIQUE INDEX uq_active_registration_per_event_user
    ON registrations (event_id, user_id)
    WHERE status NOT IN ('CANCELLED', 'NO_SHOW');

-- Capaciteitsquery index
CREATE INDEX idx_registrations_event_status
    ON registrations (event_id, status);

