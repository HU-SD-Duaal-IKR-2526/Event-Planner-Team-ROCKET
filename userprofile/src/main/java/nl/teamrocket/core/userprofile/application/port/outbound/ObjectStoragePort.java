package nl.teamrocket.core.userprofile.application.port.outbound;

/**
 * Outbound port: Object Storage (S3-compatible) for avatar uploads.
 * Context Map §6.3: User/Profile -> Object Storage is a Generic upstream relation.
 * Standard S3 API; no ACL needed because interaction is minimal (upload/download).
 */
public interface ObjectStoragePort {

    /**
     * Upload avatar bytes to Object Storage.
     * @param key         storage key (e.g. "avatars/{accountId}/{uuid}.jpg")
     * @param data        image bytes
     * @param contentType MIME type
     * @return public URL of the uploaded object
     */
    String upload(String key, byte[] data, String contentType);

    /** Delete an object by its storage key. */
    void delete(String key);
}
