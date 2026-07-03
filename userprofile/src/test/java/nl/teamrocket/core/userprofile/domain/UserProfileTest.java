package nl.teamrocket.core.userprofile.domain;

import nl.teamrocket.core.userprofile.domain.exception.*;
import nl.teamrocket.core.userprofile.domain.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UserProfile aggregate")
class UserProfileTest {

    private static final UUID OWNER    = UUID.randomUUID();
    private static final UUID STRANGER = UUID.randomUUID();

    private UserProfile fresh() {
        return UserProfile.createStub(OWNER, "jan.de.vries");
    }

    @Nested @DisplayName("createStub()")
    class CreateStub {
        @Test void derives_display_name() {
            assertThat(fresh().getDisplayName()).isEqualTo("jan de vries");
        }
        @Test void has_empty_dietary_preferences() {
            assertThat(fresh().getDietaryPreferences()).isEmpty();
        }
        @Test void has_empty_accessibility_needs() {
            assertThat(fresh().getAccessibilityNeeds()).isEmpty();
        }
        @Test void default_preferences_applied() {
            assertThat(fresh().getPreferences()).isEqualTo(Preferences.defaultPreferences());
        }
    }

    @Nested @DisplayName("update() — dietary and accessibility fields")
    class UpdateDietaryAccessibility {

        @Test void owner_can_set_dietary_preferences() {
            var p = fresh();
            p.update(OWNER, null, null, List.of("vegan", "gluten-free"), null, null);
            assertThat(p.getDietaryPreferences()).containsExactly("vegan", "gluten-free");
        }

        @Test void owner_can_set_accessibility_needs() {
            var p = fresh();
            p.update(OWNER, null, null, null, "wheelchair access required", null);
            assertThat(p.getAccessibilityNeeds()).isEqualTo("wheelchair access required");
        }

        @Test void null_dietary_leaves_existing_unchanged() {
            var p = fresh();
            p.update(OWNER, null, null, List.of("vegan"), null, null);
            p.update(OWNER, null, null, null, null, null); // null = no change
            assertThat(p.getDietaryPreferences()).containsExactly("vegan");
        }

        @Test void stranger_cannot_update() {
            assertThatThrownBy(() -> fresh().update(STRANGER, "X", null, null, null, null))
                    .isInstanceOf(ProfileAccessDeniedException.class);
        }
    }

    @Nested @DisplayName("update() — standard fields")
    class UpdateStandard {
        @Test void display_name_too_short_rejected() {
            assertThatThrownBy(() -> fresh().update(OWNER, "X", null, null, null, null))
                    .isInstanceOf(InvalidDisplayNameException.class);
        }
        @Test void bio_too_long_rejected() {
            assertThatThrownBy(() -> fresh().update(OWNER, null, "X".repeat(501), null, null, null))
                    .isInstanceOf(InvalidBioException.class);
        }
    }

    @Nested @DisplayName("Avatar invariants")
    class AvatarInvariants {
        @Test void max_2mb_enforced() {
            assertThatThrownBy(() -> Avatar.create("http://cdn/a.jpg", "image/jpeg", 2 * 1024 * 1024 + 1))
                    .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("2 MB");
        }
        @Test void disallowed_type_rejected() {
            assertThatThrownBy(() -> Avatar.create("http://cdn/a.gif", "image/gif", 100))
                    .isInstanceOf(IllegalArgumentException.class);
        }
        @Test void owner_can_set_and_remove_avatar() {
            var p = fresh();
            p.setAvatar(OWNER, Avatar.create("http://cdn/a.png", "image/png", 512));
            assertThat(p.getAvatar()).isNotNull();
            p.removeAvatar(OWNER);
            assertThat(p.getAvatar()).isNull();
        }
    }

    @Nested @DisplayName("Preferences invariants")
    class PreferencesInvariants {
        @Test void invalid_language_rejected() {
            assertThatThrownBy(() -> new Preferences("english", "Europe/Amsterdam",
                    NotificationChannel.EMAIL, true, false))
                    .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("ISO-639-1");
        }
        @Test void valid_preferences_accepted() {
            var p = new Preferences("en", "America/New_York", NotificationChannel.PUSH, false, true);
            assertThat(p.getLanguage()).isEqualTo("en");
            assertThat(p.isPushEnabled()).isTrue();
        }
    }
}
