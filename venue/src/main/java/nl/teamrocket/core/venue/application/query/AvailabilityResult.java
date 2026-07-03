package nl.teamrocket.core.venue.application.query;

import java.util.UUID;

/**
 * Antwoord op {@link CheckAvailabilityQuery}. Communicatiedoc §3.2.2:
 * response bevat {@code available} en optioneel {@code conflictingBookingId}.
 */
public record AvailabilityResult(boolean available, UUID conflictingBookingId, String reason) {

    public static AvailabilityResult ok() {
        return new AvailabilityResult(true, null, null);
    }

    public static AvailabilityResult conflict(UUID conflictingBookingId, String reason) {
        return new AvailabilityResult(false, conflictingBookingId, reason);
    }

    /** Fallback wanneer het externe systeem niet bereikbaar is (fail-closed). */
    public static AvailabilityResult unknown(String reason) {
        return new AvailabilityResult(false, null, reason);
    }
}
