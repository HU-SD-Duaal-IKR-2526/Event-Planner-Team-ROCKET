package nl.teamrocket.core.venue.domain.model;

import nl.teamrocket.core.venue.domain.exception.InvalidCapacityException;

import java.time.Instant;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Aggregate Root: Venue.
 *
 * Representeert een locatie zoals die in het Event Planner systeem bekend is.
 * Volgens de Context Map (architectuurdoc §3.2.2) is Venue een
 * <b>extern / legacy subdomein</b> waar we via een Anti-Corruption Layer een
 * lokale read-cache van bijhouden. Het externe systeem is de source of truth;
 * onze lokale kopie mag iets achterlopen (eventual consistency, data-distributiedoc
 * §2.3.2).
 *
 * Invarianten:
 *  - {@code id}, {@code name}, {@code address}, {@code capacity} zijn altijd gezet.
 *  - {@code capacity} > 0.
 *  - {@code externalVersion} is monotoon stijgend; updates met een gelijke of
 *    lagere versie worden genegeerd (idempotente cache-refresh, data-distributiedoc §4.3.2).
 *
 * EXPLICIET NIET de verantwoordelijkheid van dit aggregate:
 *  - Het maken van bookings of het bijhouden van bezettings­agenda → dat doet het
 *    externe systeem; wij geven via {@code isAvailable()} alleen door wat de ACL
 *    teruggeeft.
 *  - Het tellen van actuele deelnemers → Registration BC.
 */
public class Venue {

    private final UUID id;
    private String name;
    private Address address;
    private GeoLocation location;
    private int capacity;
    private Set<Amenity> amenities;

    /** Versie zoals geleverd door het externe systeem; basis voor dedup bij refresh. */
    private long externalVersion;

    /** Cache-metadata: wanneer hebben we deze venue voor het laatst gesynchroniseerd? */
    private Instant cachedAt;

    private Venue(UUID id, String name, Address address, GeoLocation location,
                  int capacity, Set<Amenity> amenities, long externalVersion, Instant cachedAt) {
        this.id = Objects.requireNonNull(id, "id is verplicht");
        setName(name);
        setAddress(address);
        this.location = location;
        setCapacity(capacity);
        this.amenities = amenities != null ? EnumSet.copyOf(amenities) : EnumSet.noneOf(Amenity.class);
        this.externalVersion = externalVersion;
        this.cachedAt = cachedAt;
    }

    // ── Factories ────────────────────────────────────────────────────────────

    /** Eerste keer dat we deze venue vanuit het externe systeem cachen. */
    public static Venue cacheNew(UUID id, String name, Address address, GeoLocation location,
                                 int capacity, Set<Amenity> amenities, long externalVersion) {
        return new Venue(id, name, address, location, capacity, amenities,
                externalVersion, Instant.now());
    }

    /** Reconstructie uit persistentie. */
    public static Venue reconstitute(UUID id, String name, Address address, GeoLocation location,
                                     int capacity, Set<Amenity> amenities,
                                     long externalVersion, Instant cachedAt) {
        return new Venue(id, name, address, location, capacity, amenities, externalVersion, cachedAt);
    }

    // ── Business behaviour ───────────────────────────────────────────────────

    /**
     * Werkt de lokale cache bij met een snapshot uit het externe systeem.
     * Idempotent: indien de aangeboden versie ≤ de huidige versie wordt de
     * update genegeerd (out-of-order events; data-distributiedoc §4.3.2 + §6.2).
     *
     * @return {@code true} als de cache daadwerkelijk is bijgewerkt.
     */
    public boolean refreshFromExternal(String name, Address address, GeoLocation location,
                                       int capacity, Set<Amenity> amenities,
                                       long externalVersion) {
        if (externalVersion <= this.externalVersion) {
            return false; // out-of-order of duplicaat → no-op
        }
        setName(name);
        setAddress(address);
        this.location = location;
        setCapacity(capacity);
        this.amenities = amenities != null ? EnumSet.copyOf(amenities) : EnumSet.noneOf(Amenity.class);
        this.externalVersion = externalVersion;
        this.cachedAt = Instant.now();
        return true;
    }

    /** Geeft aan of deze venue ruimte biedt voor {@code requestedHeadcount} personen. */
    public boolean hasCapacityFor(int requestedHeadcount) {
        return requestedHeadcount > 0 && requestedHeadcount <= capacity;
    }

    // ── Invariant checks ─────────────────────────────────────────────────────

    private void setName(String name) {
        if (name == null || name.isBlank() || name.length() > 200) {
            throw new IllegalArgumentException("name is verplicht en max 200 chars");
        }
        this.name = name;
    }

    private void setAddress(Address address) {
        this.address = Objects.requireNonNull(address, "address is verplicht");
    }

    private void setCapacity(int capacity) {
        if (capacity <= 0) {
            throw new InvalidCapacityException(capacity);
        }
        this.capacity = capacity;
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public UUID getId() { return id; }
    public String getName() { return name; }
    public Address getAddress() { return address; }
    public GeoLocation getLocation() { return location; }
    public int getCapacity() { return capacity; }
    public Set<Amenity> getAmenities() { return Collections.unmodifiableSet(amenities); }
    public long getExternalVersion() { return externalVersion; }
    public Instant getCachedAt() { return cachedAt; }

    @Override
    public boolean equals(Object o) {
        return o instanceof Venue v && Objects.equals(id, v.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
