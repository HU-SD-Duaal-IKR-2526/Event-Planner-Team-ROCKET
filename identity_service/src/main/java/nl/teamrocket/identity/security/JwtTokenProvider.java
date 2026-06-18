package nl.teamrocket.identity.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.*;

/**
 * Issues RS256 JWTs used as Published Language across all bounded contexts.
 *
 * Uses RSA asymmetric keys (RS256):
 *  - Identity holds the PRIVATE key and signs tokens.
 *  - All other services fetch the PUBLIC key from /.well-known/jwks.json
 *    and verify locally without calling Identity per-request.
 *
 * JWT payload (Published Language — do NOT change field names without versioning):
 *   sub            - UUID of the Account (stable identifier)
 *   iss            - "event-planner-identity"
 *   aud            - ["event-planner"]
 *   iat            - issued-at (epoch seconds)
 *   exp            - expiry (epoch seconds, 15 min default)
 *   roles          - String[] of Role names (GUEST, ORGANIZER, ADMIN, COMPLIANCE)
 *   email          - e-mail address (needed for audit + notification opt-in)
 *   correlation_id - request correlation UUID for observability
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${app.jwt.expiration-seconds:900}")
    private long expirationSeconds;

    @Value("${app.jwt.issuer:event-planner-identity}")
    private String issuer;

    @Value("${app.jwt.key-id:event-planner-key-1}")
    private String keyId;

    private RSAPrivateKey privateKey;
    private RSAPublicKey  publicKey;
    private RSAKey        rsaJwk;

    @PostConstruct
    public void init() throws Exception {
        KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        KeyPair pair = gen.generateKeyPair();
        this.privateKey = (RSAPrivateKey) pair.getPrivate();
        this.publicKey  = (RSAPublicKey)  pair.getPublic();

        // Build the JWK representation published via /.well-known/jwks.json
        this.rsaJwk = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(keyId)
                .build();

        log.info("RSA-2048 key pair generated. keyId={}, expiry={}s", keyId, expirationSeconds);
    }

    // ── Token generation ─────────────────────────────────────────────

    /**
     * Generate a signed RS256 access token.
     */
    public String generateAccessToken(UUID accountId, String email, Set<String> roles) {
        Instant now    = Instant.now();
        Instant expiry = now.plusSeconds(expirationSeconds);

        String jti = UUID.randomUUID().toString(); // unique token ID for blacklisting

        return Jwts.builder()
                .subject(accountId.toString())
                .issuer(issuer)
                .audience().add("event-planner").and()
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .id(jti)                       // jti — used for token blacklisting on logout
                .claim("email", email)
                .claim("roles", roles)
                .claim("correlation_id", CorrelationContext.current())
                .header().keyId(keyId).and()
                .signWith(privateKey)          // RS256 — asymmetric
                .compact();
    }

    /**
     * Generate a long-lived refresh token (opaque UUID stored in DB).
     * The token itself is just a random UUID; validity is checked via DB lookup.
     */
    public String generateRefreshToken() {
        return UUID.randomUUID().toString();
    }

    // ── Token validation ─────────────────────────────────────────────

    /**
     * Parse and validate an access token using the public key.
     */
    public Claims parseAndValidate(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID extractAccountId(String token) {
        return UUID.fromString(parseAndValidate(token).getSubject());
    }

    // ── JWKS endpoint ─────────────────────────────────────────────────

    /**
     * Returns the public JWK set for the /.well-known/jwks.json endpoint.
     * All other services fetch this once at startup and use it to verify
     * tokens locally — no per-request call to Identity.
     */
    public JWKSet getPublicJwkSet() {
        return new JWKSet(rsaJwk.toPublicJWK());
    }

    // ── Getters ──────────────────────────────────────────────────────

    public RSAPublicKey  getPublicKey()        { return publicKey; }
    public long          getExpirationSeconds() { return expirationSeconds; }
    public String        getIssuer()           { return issuer; }
}
