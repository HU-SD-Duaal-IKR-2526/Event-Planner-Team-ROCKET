package nl.teamrocket.core.venue.adapter.acl;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.core.venue.application.port.outbound.ExternalVenueGateway;
import nl.teamrocket.core.venue.application.query.AvailabilityResult;
import nl.teamrocket.core.venue.domain.model.Address;
import nl.teamrocket.core.venue.domain.model.Amenity;
import nl.teamrocket.core.venue.domain.model.GeoLocation;
import nl.teamrocket.core.venue.domain.model.Venue;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Stub-implementatie van de ACL outbound port {@link ExternalVenueGateway}.
 *
 * AFWIJKING T.O.V. ONTWERP: in de doelarchitectuur (architectuurdoc §3.2.2,
 * Conformist relatie naar extern legacy systeem) is dit een HTTP-client met
 * een vertaler van de externe DTO naar de interne {@link Venue}. Voor deze
 * leveringsfase zit hier een in-memory dataset met drie fictieve venues en
 * een eenvoudige beschikbaarheidssimulatie. Door dezelfde port te gebruiken
 * (hexagonaal) is later swappen door een echte HTTP-adapter een wijziging
 * uitsluitend in dit bestand.
 *
 * Gedrag van de stub:
 *  - {@link #fetch(UUID)} geeft één van de seed-venues terug of empty.
 *  - {@link #checkAvailability} markeert het uur 12-13 als bezet (lunchpauze)
 *    zodat de fail-closed-flow getest kan worden.
 */
@Slf4j
@Component
public class StubExternalVenueGateway implements ExternalVenueGateway {

    static final UUID HEX_FACTORY  = UUID.fromString("11111111-1111-1111-1111-111111111111");
    static final UUID PADUALAAN_99 = UUID.fromString("22222222-2222-2222-2222-222222222222");
    static final UUID JAARBEURS    = UUID.fromString("33333333-3333-3333-3333-333333333333");

    private final Map<UUID, Venue> remote = new HashMap<>();

    @PostConstruct
    void seed() {
        remote.put(HEX_FACTORY, Venue.cacheNew(
                HEX_FACTORY,
                "Hex Factory Utrecht",
                new Address("Croeselaan", "12", "3521 BJ", "Utrecht", "NL"),
                new GeoLocation(52.0879, 5.1099),
                250,
                EnumSet.of(Amenity.WIFI, Amenity.AV_EQUIPMENT, Amenity.STAGE, Amenity.WHEELCHAIR_ACCESSIBLE),
                1L
        ));
        remote.put(PADUALAAN_99, Venue.cacheNew(
                PADUALAAN_99,
                "HU Padualaan 99",
                new Address("Padualaan", "99", "3584 CH", "Utrecht", "NL"),
                new GeoLocation(52.0852, 5.1740),
                80,
                EnumSet.of(Amenity.WIFI, Amenity.AV_EQUIPMENT, Amenity.BREAKOUT_ROOMS),
                1L
        ));
        remote.put(JAARBEURS, Venue.cacheNew(
                JAARBEURS,
                "Jaarbeurs Hal 7",
                new Address("Jaarbeursplein", "6", "3521 AL", "Utrecht", "NL"),
                new GeoLocation(52.0888, 5.1097),
                3000,
                EnumSet.of(Amenity.WIFI, Amenity.PARKING, Amenity.CATERING,
                        Amenity.STAGE, Amenity.LIVESTREAM, Amenity.AV_EQUIPMENT),
                1L
        ));
        log.info("StubExternalVenueGateway geseed met {} venues", remote.size());
    }

    @Override
    public Optional<Venue> fetch(UUID externalId) {
        return Optional.ofNullable(remote.get(externalId));
    }

    @Override
    public AvailabilityResult checkAvailability(UUID externalId, Instant from, Instant to) {
        if (!remote.containsKey(externalId)) {
            return AvailabilityResult.conflict(null, "venue niet gevonden in extern systeem");
        }
        // Simpele regel: overlap met 12:00-13:00 UTC = lunchpauze → bezet
        Instant lunchStart = from.truncatedTo(java.time.temporal.ChronoUnit.DAYS)
                .plus(12, java.time.temporal.ChronoUnit.HOURS);
        Instant lunchEnd = lunchStart.plus(1, java.time.temporal.ChronoUnit.HOURS);
        boolean overlap = from.isBefore(lunchEnd) && to.isAfter(lunchStart);
        if (overlap) {
            return AvailabilityResult.conflict(
                    UUID.randomUUID(), "tijdslot overlapt met 12:00-13:00 UTC (lunchpauze)");
        }
        return AvailabilityResult.ok();
    }
}
