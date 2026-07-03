package nl.teamrocket.core.event.domain.model;

/**
 * Value Object: optionele, evoluerende eigenschappen van een event.
 *
 * Documentatiedoc §3: events kennen variabele metadata zoals dresscode, spreker,
 * capaciteit en livestream-link. In het domeinmodel (architectuurdoc §5.1) is dit
 * een aparte VO zodat het Event-aggregate niet vervuilt met optionele velden.
 *
 * Capaciteit hoort hier omdat het functioneel een eigenschap van het event is.
 * De daadwerkelijke capaciteitscheck bij registratie gebeurt in de Registration BC
 * (data-distributiedoc §2.4.2) — die houdt zijn eigen tellertabel.
 */
public record EventMetadata(
        String dressCode,
        String speaker,
        Integer capacity,
        String livestreamUrl
) {
    public static EventMetadata empty() {
        return new EventMetadata(null, null, null, null);
    }

    public EventMetadata withCapacity(Integer newCapacity) {
        return new EventMetadata(dressCode, speaker, newCapacity, livestreamUrl);
    }
}
