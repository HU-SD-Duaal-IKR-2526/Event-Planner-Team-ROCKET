package nl.hu.ikr.schedule.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Jackson DTO voor event.# berichten van de Event BC (Wessel).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EventLifecycleMessage {
    private String eventType;   // event.published | event.updated | event.cancelled
    private String eventId;
    private String title;
    private String startsAt;
    private String endsAt;
    private int capacity;
    private String messageId;

    public EventLifecycleMessage() {}

    public String getEventType()  { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getEventId()    { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getTitle()      { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getStartsAt()   { return startsAt; }
    public void setStartsAt(String startsAt) { this.startsAt = startsAt; }

    public String getEndsAt()     { return endsAt; }
    public void setEndsAt(String endsAt) { this.endsAt = endsAt; }

    public int getCapacity()      { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getMessageId()  { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String resolveMessageId() {
        if (messageId != null && !messageId.isBlank()) return messageId;
        return eventType + ":" + eventId;
    }
}

