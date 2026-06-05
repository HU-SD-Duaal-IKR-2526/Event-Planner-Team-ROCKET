-- V2: User Upcoming Events projectie

CREATE TABLE user_upcoming_events (
    user_id    UUID         NOT NULL,
    event_id   UUID         NOT NULL,
    title      VARCHAR(255),
    starts_at  TIMESTAMPTZ,
    CONSTRAINT pk_user_upcoming PRIMARY KEY (user_id, event_id)
);

