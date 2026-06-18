package nl.teamrocket.core.userprofile.adapter.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nl.teamrocket.core.userprofile.adapter.rest.dto.ProfileDtos.*;
import nl.teamrocket.core.userprofile.application.command.*;
import nl.teamrocket.core.userprofile.application.handler.*;
import nl.teamrocket.core.userprofile.domain.model.UserProfile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Primary adapter: REST endpoints for User/Profile module.
 * All endpoints require a valid RS256 JWT issued by the Identity service.
 * JWT "sub" claim = accountId.
 *
 * Endpoints:
 *   GET    /profiles/me                      - own profile
 *   PUT    /profiles/me                      - update own profile (incl. dietary/accessibility)
 *   PUT    /profiles/me/avatar               - set avatar (URL already uploaded to S3)
 *   DELETE /profiles/me/avatar               - remove avatar
 *   GET    /profiles/{accountId}             - any profile (authenticated)
 *   PUT    /profiles/{accountId}             - admin: update any profile
 *   GET    /users/{accountId}/notification-prefs - for Notification BC (comm. doc §3.4.4)
 */
@RestController
@RequiredArgsConstructor
public class UserProfileController {

    private final UpdateProfileHandler    updateHandler;
    private final GetProfileHandler       getHandler;
    private final AvatarHandler           avatarHandler;
    private final NotificationPrefsHandler prefsHandler;

    @GetMapping("/profiles/me")
    public ResponseEntity<ProfileResponse> getMyProfile(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(ProfileResponse.from(
                getHandler.handle(new GetProfileQuery(extractId(jwt)))));
    }

    @PutMapping("/profiles/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileRequest req) {
        UUID accountId = extractId(jwt);
        UserProfile updated = updateHandler.handle(new UpdateProfileCommand(
                accountId, accountId, req.displayName(), req.bio(),
                req.dietaryPreferences(), req.accessibilityNeeds(),
                req.language(), req.timezone(), req.notificationChannel(),
                req.emailEnabled(), req.pushEnabled()));
        return ResponseEntity.ok(ProfileResponse.from(updated));
    }

    @PutMapping("/profiles/me/avatar")
    public ResponseEntity<MessageResponse> setAvatar(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SetAvatarRequest req) {
        UUID accountId = extractId(jwt);
        avatarHandler.setAvatar(new SetAvatarCommand(
                accountId, accountId, req.storageKey(), req.url(),
                req.contentType(), req.sizeBytes()));
        return ResponseEntity.ok(new MessageResponse("Avatar bijgewerkt."));
    }

    @DeleteMapping("/profiles/me/avatar")
    public ResponseEntity<MessageResponse> removeAvatar(@AuthenticationPrincipal Jwt jwt) {
        UUID accountId = extractId(jwt);
        avatarHandler.removeAvatar(new RemoveAvatarCommand(accountId, accountId));
        return ResponseEntity.ok(new MessageResponse("Avatar verwijderd."));
    }

    @GetMapping("/profiles/{accountId}")
    public ResponseEntity<ProfileResponse> getProfile(@PathVariable UUID accountId) {
        return ResponseEntity.ok(ProfileResponse.from(
                getHandler.handle(new GetProfileQuery(accountId))));
    }

    @PutMapping("/profiles/{accountId}")
    public ResponseEntity<ProfileResponse> adminUpdateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID accountId,
            @Valid @RequestBody UpdateProfileRequest req) {
        UUID requesterId = extractId(jwt);
        UserProfile updated = updateHandler.handle(new UpdateProfileCommand(
                requesterId, accountId, req.displayName(), req.bio(),
                req.dietaryPreferences(), req.accessibilityNeeds(),
                req.language(), req.timezone(), req.notificationChannel(),
                req.emailEnabled(), req.pushEnabled()));
        return ResponseEntity.ok(ProfileResponse.from(updated));
    }

    /**
     * Notification BC queries this synchronously (HTTP GET, comm. doc §3.4.4).
     * Cached by Notification BC with 5-min TTL.
     */
    @GetMapping("/users/{accountId}/notification-prefs")
    public ResponseEntity<NotificationPrefsResponse> getNotificationPrefs(
            @PathVariable UUID accountId) {
        var result = prefsHandler.handle(new GetNotificationPrefsQuery(accountId));
        return ResponseEntity.ok(
                new NotificationPrefsResponse(result.pushEnabled(), result.emailEnabled(), result.timezone()));
    }

    private UUID extractId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}
