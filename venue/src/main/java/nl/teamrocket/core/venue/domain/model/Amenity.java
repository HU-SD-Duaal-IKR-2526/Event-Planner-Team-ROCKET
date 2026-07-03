package nl.teamrocket.core.venue.domain.model;

/**
 * Faciliteiten op een venue. Documentatiedoc §"8. Venue / Location domein"
 * vermeldt "faciliteiten" expliciet. Geen vrije strings → gecontroleerde lijst
 * voorkomt typfouten en maakt filteren mogelijk.
 */
public enum Amenity {
    WIFI,
    PARKING,
    WHEELCHAIR_ACCESSIBLE,
    CATERING,
    AV_EQUIPMENT,
    STAGE,
    LIVESTREAM,
    BREAKOUT_ROOMS
}
