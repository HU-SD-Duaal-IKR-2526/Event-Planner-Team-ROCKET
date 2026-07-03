package nl.teamrocket.core.venue.adapter.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nl.teamrocket.core.venue.adapter.rest.dto.VenueDtos.AvailabilityResponse;
import nl.teamrocket.core.venue.adapter.rest.dto.VenueDtos.RegisterVenueRequest;
import nl.teamrocket.core.venue.adapter.rest.dto.VenueDtos.VenueResponse;
import nl.teamrocket.core.venue.application.command.RefreshFromExternalCommand;
import nl.teamrocket.core.venue.application.command.RegisterVenueCommand;
import nl.teamrocket.core.venue.application.command.RemoveVenueCommand;
import nl.teamrocket.core.venue.application.port.inbound.VenueCommandPort;
import nl.teamrocket.core.venue.application.port.inbound.VenueQueryPort;
import nl.teamrocket.core.venue.application.query.CheckAvailabilityQuery;
import nl.teamrocket.core.venue.application.query.FindVenuesNearbyQuery;
import nl.teamrocket.core.venue.application.query.GetVenueQuery;
import nl.teamrocket.core.venue.domain.model.GeoLocation;
import nl.teamrocket.core.venue.domain.model.Venue;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Primary adapter: REST endpoints voor de Venue-module.
 *
 * Endpoints (communicatiedoc §3.2.2 + uitbreidingen voor admin/refresh):
 *   POST   /venues                          → register (admin/seed)
 *   GET    /venues                          → alle venues uit lokale cache
 *   GET    /venues/{id}                     → detail
 *   DELETE /venues/{id}                     → uit cache verwijderen
 *   POST   /venues/{id}/refresh             → fetch via ACL + idempotent update
 *   GET    /venues/nearby?lat&lon&radiusKm  → geospatiaal zoeken
 *   GET    /venues/{id}/availability?from&to → ACL-vraag aan extern systeem
 */
@RestController
@RequestMapping("/venues")
@RequiredArgsConstructor
public class VenueRestController {

    private final VenueCommandPort commandPort;
    private final VenueQueryPort queryPort;

    @PostMapping
    public ResponseEntity<VenueResponse> register(@Valid @RequestBody RegisterVenueRequest req) {
        Venue created = commandPort.register(new RegisterVenueCommand(
                req.externalId(),
                req.name(),
                req.address().toDomain(),
                req.location() != null ? req.location().toDomain() : null,
                req.capacity(),
                req.amenities(),
                req.externalVersion()
        ));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(VenueResponse.from(created));
    }

    @GetMapping
    public ResponseEntity<List<VenueResponse>> list() {
        return ResponseEntity.ok(queryPort.findAll().stream().map(VenueResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VenueResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(VenueResponse.from(queryPort.findById(new GetVenueQuery(id))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable UUID id) {
        commandPort.remove(new RemoveVenueCommand(id));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/refresh")
    public ResponseEntity<RefreshResponse> refresh(@PathVariable UUID id) {
        boolean updated = commandPort.refreshFromExternal(new RefreshFromExternalCommand(id));
        return ResponseEntity.ok(new RefreshResponse(updated));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<VenueResponse>> findNearby(
            @RequestParam double lat,
            @RequestParam double lon,
            @RequestParam(defaultValue = "10") double radiusKm) {
        List<VenueResponse> venues = queryPort.findNearby(
                new FindVenuesNearbyQuery(new GeoLocation(lat, lon), radiusKm))
                .stream().map(VenueResponse::from).toList();
        return ResponseEntity.ok(venues);
    }

    @GetMapping("/{id}/availability")
    public ResponseEntity<AvailabilityResponse> checkAvailability(
            @PathVariable UUID id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
        return ResponseEntity.ok(AvailabilityResponse.from(
                queryPort.checkAvailability(new CheckAvailabilityQuery(id, from, to))));
    }

    public record RefreshResponse(boolean updated) {}
}
