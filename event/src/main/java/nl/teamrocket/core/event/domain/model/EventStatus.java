package nl.teamrocket.core.event.domain.model;

/**
 * Levenscyclus van een Event aggregate.
 *
 * Toestandsdiagram (architectuurdoc §5.4.1):
 *   DRAFT → PLANNED → CANCELLED | COMPLETED
 *
 * DRAFT is toegevoegd t.o.v. de brownfield-applicatie omdat de orchestration-saga
 * (data-distributiedoc §5.2.3) een event eerst tentatief aanmaakt voordat de
 * externe venue-boeking bevestigd is.
 */
public enum EventStatus {
    DRAFT,
    PLANNED,
    CANCELLED,
    COMPLETED
}
