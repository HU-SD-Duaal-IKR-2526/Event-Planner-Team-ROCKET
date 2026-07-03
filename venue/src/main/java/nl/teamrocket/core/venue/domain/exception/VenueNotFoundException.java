package nl.teamrocket.core.venue.domain.exception;

import java.util.UUID;

public class VenueNotFoundException extends RuntimeException {
    public VenueNotFoundException(UUID venueId) {
        super("Venue niet gevonden: " + venueId);
    }
}
