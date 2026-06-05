package nl.hu.ikr.schedule.rest.dto;

import java.time.Instant;
import java.util.UUID;

public class UpcomingEventDto {
    private UUID eventId;
    private String title;
    private Instant startsAt;

    public UpcomingEventDto() {}

    public UpcomingEventDto(UUID eventId, String title, Instant startsAt) {
        this.eventId = eventId;
        this.title = title;
        this.startsAt = startsAt;
    }

    public UUID getEventId()    { return eventId; }
    public String getTitle()    { return title; }
    public Instant getStartsAt() { return startsAt; }
}

