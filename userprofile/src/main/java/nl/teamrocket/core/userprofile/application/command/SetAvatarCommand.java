package nl.teamrocket.core.userprofile.application.command;
import jakarta.validation.constraints.*;
import java.util.UUID;
public record SetAvatarCommand(
        @NotNull UUID requestingAccountId,
        @NotNull UUID targetAccountId,
        @NotBlank String storageKey,    // Object Storage key after S3 upload
        @NotBlank String url,           // public URL from Object Storage
        @NotBlank String contentType,
        long sizeBytes
) {}
