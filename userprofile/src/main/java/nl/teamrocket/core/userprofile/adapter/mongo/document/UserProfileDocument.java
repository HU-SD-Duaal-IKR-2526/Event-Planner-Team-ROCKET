package nl.teamrocket.core.userprofile.adapter.mongo.document;

import lombok.*;
import nl.teamrocket.core.userprofile.domain.model.NotificationChannel;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * MongoDB document for UserProfile aggregate.
 * Collection: profiles (in the userprofile database)
 *
 * The profile is a single document — matches the Event Storming aggregate boundary.
 * Schema-free: dietaryPreferences and accessibilityNeeds can evolve without migrations.
 * @Version: optimistic locking per data distribution doc §4.2.2.
 */
@Document(collection = "profiles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserProfileDocument {

    @Id private String id;

    @Indexed(unique = true)
    private UUID accountId;

    private String displayName;
    private String bio;

    /** Fields consumed by Registration BC via ProfileUpdated event (comm. doc §3.1.2) */
    private List<String> dietaryPreferences;
    private String       accessibilityNeeds;

    private AvatarSubDoc    avatar;
    private PreferencesSubDoc preferences;

    private Instant createdAt;
    private Instant updatedAt;

    @Version private Long version;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class AvatarSubDoc {
        private UUID   id;
        private String url;
        private String contentType;
        private long   sizeBytes;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
    public static class PreferencesSubDoc {
        private String              language;
        private String              timezone;
        private NotificationChannel notificationChannel;
        private boolean             emailEnabled;
        private boolean             pushEnabled;
    }
}
