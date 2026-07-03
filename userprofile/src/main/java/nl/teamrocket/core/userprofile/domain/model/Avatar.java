package nl.teamrocket.core.userprofile.domain.model;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Entity: Avatar image.
 * Has identity (id), can be replaced/deleted independently.
 * Invariants: max 2 MB, allowed types: jpg/png/webp, stored in Object Storage.
 * EXPLICITLY NOT: not a general attachment and not a chat attachment.
 */
public class Avatar {
    public static final long MAX_SIZE_BYTES = 2 * 1024 * 1024L;
    public static final Set<String> ALLOWED_TYPES = Set.of("image/jpeg", "image/png", "image/webp");

    private final UUID id;
    private final String url;
    private final String contentType;
    private final long sizeBytes;

    public Avatar(UUID id, String url, String contentType, long sizeBytes) {
        Objects.requireNonNull(id, "id required");
        Objects.requireNonNull(url, "url required");
        if (url.isBlank()) throw new IllegalArgumentException("Avatar URL must not be blank");
        if (!ALLOWED_TYPES.contains(contentType.toLowerCase()))
            throw new IllegalArgumentException("Avatar content type not allowed: " + contentType);
        if (sizeBytes > MAX_SIZE_BYTES)
            throw new IllegalArgumentException("Avatar exceeds 2 MB limit. Got: " + sizeBytes);
        this.id = id; this.url = url; this.contentType = contentType; this.sizeBytes = sizeBytes;
    }

    public static Avatar create(String url, String contentType, long sizeBytes) {
        return new Avatar(UUID.randomUUID(), url, contentType, sizeBytes);
    }

    public UUID getId() { return id; }
    public String getUrl() { return url; }
    public String getContentType() { return contentType; }
    public long getSizeBytes() { return sizeBytes; }

    @Override public boolean equals(Object o) { return o instanceof Avatar a && Objects.equals(id, a.id); }
    @Override public int hashCode() { return Objects.hash(id); }
}
