package nl.teamrocket.core.userprofile.adapter;

import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.core.userprofile.application.port.outbound.ObjectStoragePort;
import nl.teamrocket.core.userprofile.domain.exception.AvatarUploadException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * Secondary adapter: Object Storage (S3-compatible) for avatar uploads.
 *
 * Context Map §6.3: User/Profile → Object Storage is a Generic upstream.
 * Standard S3 API; no ACL needed because interaction is minimal (upload/download).
 *
 * Works with AWS S3, MinIO, or any S3-compatible store.
 * The REST controller uploads the file, gets the URL, then passes it to the handler.
 *
 * For local development: run MinIO with docker-compose.
 */
@Slf4j
@Component
public class S3ObjectStorageAdapter implements ObjectStoragePort {

    private final S3Client s3Client;

    @Value("${app.storage.bucket:eventplanner-avatars}")
    private String bucket;

    @Value("${app.storage.base-url:http://localhost:9000/eventplanner-avatars}")
    private String baseUrl;

    public S3ObjectStorageAdapter(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    @Override
    public String upload(String key, byte[] data, String contentType) {
        try {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucket)
                            .key(key)
                            .contentType(contentType)
                            .contentLength((long) data.length)
                            .build(),
                    RequestBody.fromBytes(data)
            );
            String url = baseUrl + "/" + key;
            log.info("Avatar uploaded to Object Storage: key={}", key);
            return url;
        } catch (Exception e) {
            throw new AvatarUploadException("S3 upload failed for key " + key + ": " + e.getMessage());
        }
    }

    @Override
    public void delete(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
            log.info("Avatar deleted from Object Storage: key={}", key);
        } catch (Exception e) {
            log.warn("Failed to delete avatar key={} from Object Storage: {}", key, e.getMessage());
        }
    }
}
