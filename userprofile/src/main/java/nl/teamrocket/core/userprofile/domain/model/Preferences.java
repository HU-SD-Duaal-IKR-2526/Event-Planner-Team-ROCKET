package nl.teamrocket.core.userprofile.domain.model;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Value Object: immutable user preferences.
 * Invariants: language = ISO-639-1, timezone = IANA id, channel in {IN_APP, PUSH, EMAIL}.
 * Immutable: when changed the entire VO is replaced (not mutated).
 * EXPLICITLY NOT: not notification subscriptions — those live in Notification BC.
 */
public final class Preferences {

    private static final Pattern ISO_639_1 = Pattern.compile("^[a-z]{2}$");

    private final String language;
    private final String timezone;
    private final NotificationChannel notificationChannel;
    private final boolean emailEnabled;
    private final boolean pushEnabled;

    public Preferences(String language, String timezone,
                       NotificationChannel notificationChannel,
                       boolean emailEnabled, boolean pushEnabled) {
        Objects.requireNonNull(language, "Language required");
        Objects.requireNonNull(timezone, "Timezone required");
        Objects.requireNonNull(notificationChannel, "NotificationChannel required");
        if (!ISO_639_1.matcher(language).matches())
            throw new IllegalArgumentException("Language must be ISO-639-1, got: " + language);
        if (timezone.isBlank())
            throw new IllegalArgumentException("Timezone must not be blank");
        this.language = language;
        this.timezone = timezone;
        this.notificationChannel = notificationChannel;
        this.emailEnabled = emailEnabled;
        this.pushEnabled = pushEnabled;
    }

    public static Preferences defaultPreferences() {
        return new Preferences("nl", "Europe/Amsterdam", NotificationChannel.EMAIL, true, false);
    }

    public String getLanguage() { return language; }
    public String getTimezone() { return timezone; }
    public NotificationChannel getNotificationChannel() { return notificationChannel; }
    public boolean isEmailEnabled() { return emailEnabled; }
    public boolean isPushEnabled() { return pushEnabled; }

    @Override public boolean equals(Object o) {
        if (!(o instanceof Preferences p)) return false;
        return emailEnabled == p.emailEnabled && pushEnabled == p.pushEnabled
                && Objects.equals(language, p.language)
                && Objects.equals(timezone, p.timezone)
                && notificationChannel == p.notificationChannel;
    }
    @Override public int hashCode() { return Objects.hash(language, timezone, notificationChannel, emailEnabled, pushEnabled); }
}
