package nl.hu.ikr.schedule.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RegistrationEventMessage {
    private String registrationId;
    private String eventId;
    private String userId;
    private String status;
    private int guestCount;
    private String reservedAt;
    private String confirmedAt;
    private String waitlistedAt;
    private String cancelledAt;
    private String messageId;

    public RegistrationEventMessage() {}

    public String getRegistrationId() { return registrationId; }
    public void setRegistrationId(String registrationId) { this.registrationId = registrationId; }

    public String getEventId()  { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getUserId()   { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getStatus()   { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getGuestCount()  { return guestCount; }
    public void setGuestCount(int guestCount) { this.guestCount = guestCount; }

    public String getReservedAt()   { return reservedAt; }
    public void setReservedAt(String reservedAt) { this.reservedAt = reservedAt; }

    public String getConfirmedAt()  { return confirmedAt; }
    public void setConfirmedAt(String confirmedAt) { this.confirmedAt = confirmedAt; }

    public String getWaitlistedAt() { return waitlistedAt; }
    public void setWaitlistedAt(String waitlistedAt) { this.waitlistedAt = waitlistedAt; }

    public String getCancelledAt()  { return cancelledAt; }
    public void setCancelledAt(String cancelledAt) { this.cancelledAt = cancelledAt; }

    public String getMessageId()    { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String resolveMessageId(String routingKey) {
        if (messageId != null && !messageId.isBlank()) return messageId;
        return routingKey + ":" + registrationId;
    }
}
