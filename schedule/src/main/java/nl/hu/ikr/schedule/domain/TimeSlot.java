package nl.hu.ikr.schedule.domain;

import java.time.Instant;

/**
 * Value Object: een tijdslot met een begin en einde.
 */
public class TimeSlot {

    private final Instant start;
    private final Instant end;

    public TimeSlot(Instant start, Instant end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("start en end mogen niet null zijn");
        }
        if (!start.isBefore(end)) {
            throw new IllegalArgumentException("start moet vóór end liggen");
        }
        this.start = start;
        this.end = end;
    }

    /**
     * Detecteert of dit slot overlapt met een ander slot.
     * Aangrenzende slots (end == other.start) gelden NIET als overlap.
     */
    public boolean overlapsWith(TimeSlot other) {
        return this.start.isBefore(other.end) && other.start.isBefore(this.end);
    }

    public Instant getStart() { return start; }
    public Instant getEnd()   { return end; }

    @Override
    public String toString() {
        return "[" + start + " – " + end + "]";
    }
}

