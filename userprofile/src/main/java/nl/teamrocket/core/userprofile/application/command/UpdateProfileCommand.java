package nl.teamrocket.core.userprofile.application.command;

import nl.teamrocket.core.userprofile.domain.model.NotificationChannel;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

/**
 * Update own profile.
 * Communication doc §3.1.2: dietaryPreferences and accessibilityNeeds are
 * part of the ProfileUpdated event contract consumed by Registration BC.
 * Null = no change for that field.
 */
public record UpdateProfileCommand(
        UUID   requestingAccountId,
        UUID   targetAccountId,
        @Size(min = 2, max = 64)  String displayName,
        @Size(max = 500)           String bio,
        List<String>               dietaryPreferences,   // e.g. ["vegan","gluten-free"]
        String                     accessibilityNeeds,   // free text
        String                     language,
        String                     timezone,
        NotificationChannel        notificationChannel,
        Boolean                    emailEnabled,
        Boolean                    pushEnabled
) {}
