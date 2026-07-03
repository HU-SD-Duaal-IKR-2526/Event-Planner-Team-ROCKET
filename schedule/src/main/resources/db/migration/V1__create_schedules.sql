-- V1: Schedules en Sessions schema

CREATE TABLE schedules (
    id               UUID         NOT NULL,
    event_id         UUID         NOT NULL,
    event_title      VARCHAR(255),
    event_starts_at  TIMESTAMPTZ,
    event_ends_at    TIMESTAMPTZ,
    headcount        INT          NOT NULL DEFAULT 0,
    last_updated     TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    version          BIGINT       NOT NULL DEFAULT 0,
    CONSTRAINT pk_schedules PRIMARY KEY (id),
    CONSTRAINT uq_schedule_event_id UNIQUE (event_id)
);

CREATE TABLE sessions (
    id           UUID         NOT NULL,
    schedule_id  UUID         NOT NULL,
    title        VARCHAR(255) NOT NULL,
    room_id      UUID         NOT NULL,
    speaker_id   UUID         NOT NULL,
    slot_start   TIMESTAMPTZ  NOT NULL,
    slot_end     TIMESTAMPTZ  NOT NULL,
    CONSTRAINT pk_sessions PRIMARY KEY (id),
    CONSTRAINT fk_sessions_schedule FOREIGN KEY (schedule_id) REFERENCES schedules(id) ON DELETE CASCADE,
    CONSTRAINT chk_slot_order CHECK (slot_start < slot_end)
);

-- Overlap-detectie queries
CREATE INDEX idx_sessions_room_slot
    ON sessions (room_id, slot_start, slot_end);

CREATE INDEX idx_sessions_speaker_slot
    ON sessions (speaker_id, slot_start, slot_end);

