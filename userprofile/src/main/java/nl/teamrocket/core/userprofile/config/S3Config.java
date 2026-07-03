package nl.teamrocket.core.userprofile.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * S3-compatible Object Storage config.
 * Works with AWS S3 or local MinIO (docker-compose).
 * Context Map §6.3: User/Profile → Object Storage (Generic upstream, no ACL).
 */
@Configuration
public class S3Config {

    @Value("${app.storage.endpoint:http://localhost:9000}")
    private String endpoint;

    @Value("${app.storage.access-key:minioadmin}")
    private String accessKey;

    @Value("${app.storage.secret-key:minioadmin}")
    private String secretKey;

    @Value("${app.storage.region:us-east-1}")
    private String region;

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .forcePathStyle(true)   // required for MinIO + S3-compatible stores
                .build();
    }
}
