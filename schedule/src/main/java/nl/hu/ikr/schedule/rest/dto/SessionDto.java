package nl.hu.ikr.schedule.rest.dto;

import java.time.Instant;
import java.util.UUID;

public class SessionDto {
    private UUID sessionId;
    private String title;
    private Instant start;
    private Instant end;
    private UUID roomId;
    private UUID speakerId;

    public SessionDto() {}

    public SessionDto(UUID sessionId, String title, Instant start, Instant end,
                      UUID roomId, UUID speakerId) {
        this.sessionId = sessionId;
        this.title = title;
        this.start = start;
        this.end = end;
        this.roomId = roomId;
        this.speakerId = speakerId;
    }

    public UUID getSessionId()   { return sessionId; }
    public String getTitle()     { return title; }
    public Instant getStart()    { return start; }
    public Instant getEnd()      { return end; }
    public UUID getRoomId()      { return roomId; }
    public UUID getSpeakerId()   { return speakerId; }
}

