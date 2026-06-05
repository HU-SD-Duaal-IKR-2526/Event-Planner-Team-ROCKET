package nl.hu.ikr.registration.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.UUID;

/**
 * Jackson DTO voor inkomende event.# berichten van de Event BC (Wessel).
 * ignoreUnknown=true zodat extra velden (title, startsAt, endsAt, etc.) geen exception gooien.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventSyncMessage {
    private String eventType;   // event.published | event.updated | event.cancelled
    private UUID eventId;
    private int capacity;
    private boolean open;

    public EventSyncMessage() {}

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }
}

