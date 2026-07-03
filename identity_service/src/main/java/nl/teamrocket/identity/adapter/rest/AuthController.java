package nl.teamrocket.identity.adapter.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import nl.teamrocket.identity.adapter.rest.dto.IdentityDtos.*;
import nl.teamrocket.identity.application.command.*;
import nl.teamrocket.identity.application.handler.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Primary adapter: REST endpoints for authentication flows.
 *
 * Endpoints:
 *   POST /auth/register                   - create account, trigger verification e-mail via event
 *   POST /auth/login                      - authenticate → access token (JWT RS256) + refresh token
 *   POST /auth/refresh                    - rotate refresh token → new access + refresh tokens
 *   POST /auth/logout                     - blacklist access token jti + revoke refresh token
 *   POST /auth/verify-email               - verify e-mail with token
 *   POST /auth/resend-verification        - request a new verification e-mail
 *   POST /auth/password-reset/request     - initiate password reset (event to Email BC)
 *   POST /auth/password-reset/confirm     - apply new password with reset token
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RegisterAccountHandler registerHandler;
    private final LoginHandler loginHandler;
    private final RefreshTokenHandler refreshTokenHandler;
    private final VerifyEmailHandler verifyEmailHandler;
    private final RequestPasswordResetHandler requestResetHandler;
    private final ResetPasswordHandler resetPasswordHandler;
    private final ResendVerificationHandler resendVerificationHandler;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest req) {
        var accountId = registerHandler.handle(new RegisterAccountCommand(req.email(), req.password()));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegisterResponse(accountId,
                        "Account aangemaakt. Controleer je e-mail om je account te activeren."));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        LoginResult result = loginHandler.handle(new LoginCommand(req.email(), req.password()));
        return ResponseEntity.ok(toResponse(result));
    }

    /**
     * Token rotation: exchange an unexpired refresh token for a new access + refresh token pair.
     * Old refresh token is immediately revoked (rotation prevents replay attacks).
     */
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@Valid @RequestBody RefreshRequest req) {
        LoginResult result = refreshTokenHandler.refresh(new RefreshTokenCommand(req.refreshToken()));
        return ResponseEntity.ok(toResponse(result));
    }

    /**
     * Logout: blacklist the current access token and revoke the refresh token.
     * Even if the JWT has not expired, it will be rejected by TokenBlacklistFilter.
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody LogoutRequest req) {
        String jti = jwt != null ? jwt.getId() : req.accessTokenJti();
        UUID accountId = jwt != null ? UUID.fromString(jwt.getSubject()) : null;
        refreshTokenHandler.logout(new LogoutCommand(accountId, jti, req.refreshToken()));
        return ResponseEntity.ok(new MessageResponse("Succesvol uitgelogd."));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@Valid @RequestBody VerifyEmailRequest req) {
        verifyEmailHandler.handle(new VerifyEmailCommand(req.token()));
        return ResponseEntity.ok(new MessageResponse("E-mailadres succesvol geverifieerd."));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(
            @Valid @RequestBody ResendVerificationRequest req) {
        resendVerificationHandler.handle(new ResendVerificationCommand(req.email()));
        return ResponseEntity.ok(new MessageResponse(
                "Als het account bestaat en nog niet geverifieerd is, is een nieuwe e-mail verstuurd."));
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<MessageResponse> requestReset(
            @Valid @RequestBody RequestPasswordResetRequest req) {
        requestResetHandler.handle(new RequestPasswordResetCommand(req.email()));
        return ResponseEntity.ok(new MessageResponse(
                "Als het account bestaat, is een reset-link verstuurd."));
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<MessageResponse> confirmReset(@Valid @RequestBody ResetPasswordRequest req) {
        resetPasswordHandler.handle(new ResetPasswordCommand(req.token(), req.newPassword()));
        return ResponseEntity.ok(new MessageResponse("Wachtwoord succesvol gewijzigd."));
    }

    private LoginResponse toResponse(LoginResult r) {
        return new LoginResponse(r.accountId(), r.email(), r.roles(),
                r.accessToken(), r.expiresInSeconds(), r.refreshToken(), "Bearer");
    }
}
