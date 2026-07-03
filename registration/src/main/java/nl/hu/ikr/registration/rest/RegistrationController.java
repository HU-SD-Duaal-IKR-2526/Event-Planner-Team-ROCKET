package nl.hu.ikr.registration.rest;

import jakarta.validation.Valid;
import nl.hu.ikr.registration.application.ReserveRegistrationHandler;
import nl.hu.ikr.registration.application.dto.RegistrationResult;
import nl.hu.ikr.registration.application.dto.ReserveRegistrationCommand;
import nl.hu.ikr.registration.rest.dto.RegistrationRequest;
import nl.hu.ikr.registration.rest.dto.RegistrationResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * POST /registrations — synchronous command+result (gebruiker wacht in UI).
 * Headers:
 *   X-Idempotency-Key  (verplicht, client-gegenereerde UUID)
 *   X-User-Id          (verplicht, UUID gezet door API Gateway na JWT-validatie)
 */
@RestController
@RequestMapping("/registrations")
public class RegistrationController {

    private final ReserveRegistrationHandler handler;

    public RegistrationController(ReserveRegistrationHandler handler) {
        this.handler = handler;
    }

    @PostMapping
    public ResponseEntity<RegistrationResponse> reserve(
            @RequestHeader("X-Idempotency-Key") String idempotencyKey,
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody RegistrationRequest request) {

        ReserveRegistrationCommand cmd = new ReserveRegistrationCommand(
                request.getEventId(),
                userId,
                idempotencyKey,
                request.getPlusOnes(),
                request.getNotes(),
                request.getChannel() != null ? request.getChannel() : "WEB"
        );

        RegistrationResult result = handler.handle(cmd);
        RegistrationResponse response = RegistrationResponse.from(result);

        return ResponseEntity
                .status(result.isNew() ? HttpStatus.CREATED : HttpStatus.OK)
                .body(response);
    }

    // ── Fout-afhandeling ──────────────────────────────────────────────────────

    @ExceptionHandler(ReserveRegistrationHandler.EventNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ReserveRegistrationHandler.EventNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
    }

    @ExceptionHandler(ReserveRegistrationHandler.EventNotOpenException.class)
    public ResponseEntity<String> handleNotOpen(ReserveRegistrationHandler.EventNotOpenException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<String> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
    }
}

