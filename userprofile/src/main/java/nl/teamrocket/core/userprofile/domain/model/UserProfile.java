package nl.teamrocket.core.userprofile.domain.model;

import nl.teamrocket.core.userprofile.domain.exception.*;

import java.time.Instant;
import java.util.*;

/**
 * Aggregate Root: UserProfile.
 *
 * Personal presentation layer: name, bio, avatar, dietary preferences,
 * accessibility needs, notification preferences.
 *
 * Invariants:
 *  - 1:1 with Account via accountId (cross-aggregate reference by ID only — never the Account object)
 *  - DisplayName: 2–64 characters
 *  - Bio: max 500 characters
 *  - Only the profile owner may modify it; admins have a separate command
 *  - dietaryPreferences and accessibilityNeeds published via ProfileUpdated event
 *    for Registration BC's guest-list read model
 *
 * EXPLICITLY NOT: UserProfile is NOT the Account. Deleting a UserProfile does NOT
 *                 delete the Account. Authentication lives in the Identity BC.
 */
public class UserProfile {

    private static final int DISPLAY_NAME_MIN = 2;
    private static final int DISPLAY_NAME_MAX = 64;
    private static final int BIO_MAX          = 500;

    // Identity
    private final String id;           // MongoDB _id = accountId string
    private final UUID   accountId;    // Cross-aggregate reference to Identity BC

    // Profile fields
    private String displayName;
    private String bio;
    private Avatar avatar;
    private Preferences preferences;

    // Fields consumed by Registration BC via ProfileUpdated event
    // Communication doc §3.1.2: dietaryPreferences and accessibilityNeeds are
    // explicitly part of the ProfileUpdated Published Language contract.
    private List<String> dietaryPreferences;   // e.g. ["vegan", "gluten-free"]
    private String       accessibilityNeeds;   // free text or codes

    // Audit
    private final Instant createdAt;
    private Instant updatedAt;
    private Long version;

    // Domain events
    private final List<Object> domainEvents = new ArrayList<>();

    private UserProfile(String id, UUID accountId, String displayName, Instant createdAt) {
        this.id = id;
        this.accountId = accountId;
        this.displayName = displayName;
        this.bio = "";
        this.preferences = Preferences.defaultPreferences();
        this.avatar = null;
        this.dietaryPreferences = new ArrayList<>();
        this.accessibilityNeeds = "";
        this.createdAt = createdAt;
        this.updatedAt = createdAt;
        this.version = null;
    }

    // ── Factory methods ──────────────────────────────────────────

    public static UserProfile createStub(UUID accountId, String emailLocalPart) {
        Objects.requireNonNull(accountId, "accountId required");
        Instant now = Instant.now();
        return new UserProfile(accountId.toString(), accountId,
                deriveDisplayName(emailLocalPart), now);
    }

    public static UserProfile reconstitute(
            String id, UUID accountId, String displayName,
            String bio, Avatar avatar, Preferences preferences,
            List<String> dietaryPreferences, String accessibilityNeeds,
            Instant createdAt, Instant updatedAt, Long version) {
        UserProfile p = new UserProfile(id, accountId, displayName, createdAt);
        p.bio = bio != null ? bio : "";
        p.avatar = avatar;
        p.preferences = preferences != null ? preferences : Preferences.defaultPreferences();
        p.dietaryPreferences = dietaryPreferences != null ? new ArrayList<>(dietaryPreferences) : new ArrayList<>();
        p.accessibilityNeeds = accessibilityNeeds != null ? accessibilityNeeds : "";
        p.updatedAt = updatedAt;
        p.version = version;
        return p;
    }

    // ── Business behaviour ───────────────────────────────────────

    /**
     * Owner updates their own profile.
     * Pass null for any field you do not want to change.
     */
    public void update(UUID requestingAccountId, String newDisplayName, String newBio,
                       List<String> newDietaryPreferences, String newAccessibilityNeeds,
                       Preferences newPreferences) {
        assertOwner(requestingAccountId);
        applyUpdates(newDisplayName, newBio, newDietaryPreferences, newAccessibilityNeeds, newPreferences);
    }

    /**
     * Admin updates a profile without ownership check.
     */
    public void adminUpdate(UUID adminId, String newDisplayName, String newBio) {
        // No ownership check — admin privilege
        if (newDisplayName != null) { validateDisplayName(newDisplayName); this.displayName = newDisplayName; }
        if (newBio != null) { validateBio(newBio); this.bio = newBio; }
        this.updatedAt = Instant.now();
    }

    public void setAvatar(UUID requestingAccountId, Avatar newAvatar) {
        assertOwner(requestingAccountId);
        Objects.requireNonNull(newAvatar, "Avatar required");
        this.avatar = newAvatar;
        this.updatedAt = Instant.now();
    }

    public void removeAvatar(UUID requestingAccountId) {
        assertOwner(requestingAccountId);
        this.avatar = null;
        this.updatedAt = Instant.now();
    }

    // ── Private helpers ──────────────────────────────────────────

    private void applyUpdates(String newDisplayName, String newBio,
                               List<String> newDietaryPreferences, String newAccessibilityNeeds,
                               Preferences newPreferences) {
        if (newDisplayName != null) { validateDisplayName(newDisplayName); this.displayName = newDisplayName; }
        if (newBio != null) { validateBio(newBio); this.bio = newBio; }
        if (newDietaryPreferences != null) this.dietaryPreferences = new ArrayList<>(newDietaryPreferences);
        if (newAccessibilityNeeds != null) this.accessibilityNeeds = newAccessibilityNeeds;
        if (newPreferences != null) this.preferences = newPreferences;
        this.updatedAt = Instant.now();
    }

    private void assertOwner(UUID requestingAccountId) {
        if (!accountId.equals(requestingAccountId))
            throw new ProfileAccessDeniedException(accountId, requestingAccountId);
    }

    private static void validateDisplayName(String name) {
        if (name.length() < DISPLAY_NAME_MIN || name.length() > DISPLAY_NAME_MAX)
            throw new InvalidDisplayNameException(name, DISPLAY_NAME_MIN, DISPLAY_NAME_MAX);
    }

    private static void validateBio(String bio) {
        if (bio.length() > BIO_MAX)
            throw new InvalidBioException(bio.length(), BIO_MAX);
    }

    private static String deriveDisplayName(String emailLocalPart) {
        if (emailLocalPart == null || emailLocalPart.isBlank()) return "User";
        String name = emailLocalPart.replace(".", " ").replace("_", " ").trim();
        if (name.length() < DISPLAY_NAME_MIN) name = name + "User";
        if (name.length() > DISPLAY_NAME_MAX) name = name.substring(0, DISPLAY_NAME_MAX);
        return name;
    }

    // ── Domain events ────────────────────────────────────────────

    public void registerEvent(Object event) { domainEvents.add(event); }
    public List<Object> pullDomainEvents() {
        List<Object> e = new ArrayList<>(domainEvents);
        domainEvents.clear();
        return e;
    }

    // ── Getters ──────────────────────────────────────────────────

    public String getId() { return id; }
    public UUID getAccountId() { return accountId; }
    public String getDisplayName() { return displayName; }
    public String getBio() { return bio; }
    public Avatar getAvatar() { return avatar; }
    public Preferences getPreferences() { return preferences; }
    public List<String> getDietaryPreferences() { return Collections.unmodifiableList(dietaryPreferences); }
    public String getAccessibilityNeeds() { return accessibilityNeeds; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Long getVersion() { return version; }
    public void setVersion(Long v) { this.version = v; }

    @Override public boolean equals(Object o) { return o instanceof UserProfile p && Objects.equals(id, p.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
