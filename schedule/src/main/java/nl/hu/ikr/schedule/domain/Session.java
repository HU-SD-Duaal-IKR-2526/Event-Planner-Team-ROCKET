package nl.hu.ikr.schedule.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * Sessie-entiteit — onderdeel van het Schedule aggregate.
 */
@Entity
@Table(name = "sessions")
public class Session {

    @Id
    private UUID id;

    @Column(name = "schedule_id", nullable = false)
    private UUID scheduleId;

    @Column(nullable = false)
    private String title;

    @Column(name = "room_id", nullable = false)
    private UUID roomId;

    @Column(name = "speaker_id", nullable = false)
    private UUID speakerId;

    @Column(name = "slot_start", nullable = false)
    private Instant slotStart;

    @Column(name = "slot_end", nullable = false)
    private Instant slotEnd;

    protected Session() {}

    public Session(UUID scheduleId, String title, UUID roomId, UUID speakerId, TimeSlot slot) {
        this.id = UUID.randomUUID();
        this.scheduleId = scheduleId;
        this.title = title;
        this.roomId = roomId;
        this.speakerId = speakerId;
        this.slotStart = slot.getStart();
        this.slotEnd = slot.getEnd();
    }

    public boolean roomOverlapsWith(Session other) {
        return this.roomId.equals(other.roomId) && this.getSlot().overlapsWith(other.getSlot());
    }

    public boolean speakerOverlapsWith(Session other) {
        return this.speakerId.equals(other.speakerId) && this.getSlot().overlapsWith(other.getSlot());
    }

    public TimeSlot getSlot() {
        return new TimeSlot(slotStart, slotEnd);
    }

    public UUID getId()         { return id; }
    public UUID getScheduleId() { return scheduleId; }
    public String getTitle()    { return title; }
    public UUID getRoomId()     { return roomId; }
    public UUID getSpeakerId()  { return speakerId; }
    public Instant getSlotStart() { return slotStart; }
    public Instant getSlotEnd()   { return slotEnd; }
}

