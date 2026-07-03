package nl.teamrocket.core.venue.application.port.inbound;

import nl.teamrocket.core.venue.application.command.RefreshFromExternalCommand;
import nl.teamrocket.core.venue.application.command.RegisterVenueCommand;
import nl.teamrocket.core.venue.application.command.RemoveVenueCommand;
import nl.teamrocket.core.venue.domain.model.Venue;

/**
 * Inbound port: use-cases die de lokale venue-cache muteren.
 */
public interface VenueCommandPort {

    Venue register(RegisterVenueCommand command);

    /**
     * Refresht uit het externe systeem via de ACL. Idempotent op externalVersion.
     * @return {@code true} als de cache daadwerkelijk is bijgewerkt.
     */
    boolean refreshFromExternal(RefreshFromExternalCommand command);

    void remove(RemoveVenueCommand command);
}
