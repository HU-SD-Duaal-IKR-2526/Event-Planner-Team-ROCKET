package nl.hu.ikr.registration.rest.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class RegistrationRequest {

    @NotNull(message = "eventId is verplicht")
    private UUID eventId;

    @Min(value = 0, message = "plusOnes mag niet negatief zijn")
    private int plusOnes = 0;

    private String notes;

    private String channel = "WEB";

    public RegistrationRequest() {}

    public UUID getEventId()       { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }

    public int getPlusOnes()       { return plusOnes; }
    public void setPlusOnes(int plusOnes) { this.plusOnes = plusOnes; }

    public String getNotes()       { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getChannel()     { return channel; }
    public void setChannel(String channel) { this.channel = channel; }
}

