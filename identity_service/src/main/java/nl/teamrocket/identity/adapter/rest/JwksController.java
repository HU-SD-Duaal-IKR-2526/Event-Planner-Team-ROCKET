package nl.teamrocket.identity.adapter.rest;

import lombok.RequiredArgsConstructor;
import nl.teamrocket.identity.security.JwtTokenProvider;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Exposes the RSA public key as a JWKS endpoint.
 *
 * All other bounded context services call GET /.well-known/jwks.json
 * once at startup and cache the public key. They then verify every
 * incoming JWT locally — no per-request call to Identity.
 *
 * This is the core of the OHS + Published Language pattern for Identity.
 */
@RestController
@RequiredArgsConstructor
public class JwksController {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * GET /.well-known/jwks.json
     * Returns the public RSA key as a JSON Web Key Set.
     * Responses are cacheable (public, no-store not needed — key rarely rotates).
     */
    @GetMapping(value = "/.well-known/jwks.json", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Map<String, Object>> jwks() {
        return ResponseEntity.ok()
                .header("Cache-Control", "public, max-age=21600") // 6 hours
                .body(jwtTokenProvider.getPublicJwkSet().toJSONObject());
    }
}
