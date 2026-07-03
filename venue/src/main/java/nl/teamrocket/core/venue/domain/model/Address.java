package nl.teamrocket.core.venue.domain.model;

import java.util.Objects;

/**
 * Value Object: postadres van een venue. Komt 1:1 uit het externe systeem
 * (Architectuurdoc §3.2.2: Conformist relatie). Alleen {@code city} en
 * {@code country} zijn verplicht — straat/huisnummer ontbreken soms bij online
 * locaties of grote complexen.
 */
public record Address(
        String street,
        String houseNumber,
        String postalCode,
        String city,
        String country
) {
    public Address {
        Objects.requireNonNull(city, "city is verplicht");
        Objects.requireNonNull(country, "country is verplicht");
    }

    public String formatted() {
        StringBuilder sb = new StringBuilder();
        if (street != null && !street.isBlank()) {
            sb.append(street);
            if (houseNumber != null && !houseNumber.isBlank()) {
                sb.append(' ').append(houseNumber);
            }
            sb.append(", ");
        }
        if (postalCode != null && !postalCode.isBlank()) {
            sb.append(postalCode).append(' ');
        }
        sb.append(city).append(", ").append(country);
        return sb.toString();
    }
}
