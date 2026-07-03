package nl.hu.ikr.schedule.rest.dto;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class EventScheduleResponse {
    private UUID eventId;
    private String title;
    private List<SessionDto> timeline;
    private int headcount;
    private Instant lastUpdated;

    public EventScheduleResponse() {}

    public EventScheduleResponse(UUID eventId, String title, List<SessionDto> timeline,
                                  int headcount, Instant lastUpdated) {
        this.eventId = eventId;
        this.title = title;
        this.timeline = timeline;
        this.headcount = headcount;
        this.lastUpdated = lastUpdated;
    }

    public UUID getEventId()         { return eventId; }
    public String getTitle()         { return title; }
    public List<SessionDto> getTimeline() { return timeline; }
    public int getHeadcount()        { return headcount; }
    public Instant getLastUpdated()  { return lastUpdated; }
}

