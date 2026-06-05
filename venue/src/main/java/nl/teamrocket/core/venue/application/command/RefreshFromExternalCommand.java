package nl.teamrocket.core.venue.application.command;

import java.util.UUID;

/**
 * Triggert een ACL-fetch vanuit het externe venue-systeem en werkt de lokale
 * cache idempotent bij. Aangeroepen door admin endpoint of door een scheduler
 * voor cache-refresh (data-distributiedoc §4.3.2).
 */
public record RefreshFromExternalCommand(UUID externalId) {}
