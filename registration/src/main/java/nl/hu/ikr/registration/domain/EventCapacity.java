package nl.hu.ikr.registration.domain;

import jakarta.persistence.*;
import java.util.UUID;

/**
 * Lokaal read-model van event-capaciteit.
 * Gevoed door Event BC events (event.published / event.updated / event.cancelled).
 */
@Entity
@Table(name = "event_capacity")
public class EventCapacity {

    @Id
    @Column(name = "event_id")
    private UUID eventId;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private boolean open;

    protected EventCapacity() {}

    public EventCapacity(UUID eventId, int capacity, boolean open) {
        this.eventId = eventId;
        this.capacity = capacity;
        this.open = open;
    }

    /**
     * @param takenSeats aantal RESERVED + CONFIRMED seats
     * @param plusOnes   extra gasten die bij deze registratie horen
     * @return true als er nog ruimte is
     */
    public boolean hasRoom(int takenSeats, int plusOnes) {
        return (takenSeats + plusOnes + 1) <= capacity;
    }

    public boolean isOpen()      { return open; }
    public UUID getEventId()     { return eventId; }
    public int getCapacity()     { return capacity; }

    public void setCapacity(int capacity) { this.capacity = capacity; }
    public void setOpen(boolean open)     { this.open = open; }
}

