package nl.teamrocket.core.venue.application.command;

import nl.teamrocket.core.venue.domain.model.Address;
import nl.teamrocket.core.venue.domain.model.Amenity;
import nl.teamrocket.core.venue.domain.model.GeoLocation;

import java.util.Set;
import java.util.UUID;

/**
 * Command om handmatig een venue in de lokale cache te registreren. In de
 * doelarchitectuur worden venues primair via {@code RefreshFromExternalCommand}
 * gesynchroniseerd; dit endpoint is bedoeld voor admin-flows en seed-data
 * (documentatiedoc §"VEN01: locatie toevoegen").
 */
public record RegisterVenueCommand(
        UUID externalId,
        String name,
        Address address,
        GeoLocation location,
        int capacity,
        Set<Amenity> amenities,
        long externalVersion
) {}
