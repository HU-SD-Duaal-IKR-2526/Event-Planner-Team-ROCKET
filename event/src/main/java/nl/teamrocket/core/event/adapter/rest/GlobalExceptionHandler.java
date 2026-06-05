package nl.teamrocket.core.event.adapter.rest;

import nl.teamrocket.core.event.adapter.rest.dto.EventDtos.ApiError;
import nl.teamrocket.core.event.domain.exception.EventNotFoundException;
import nl.teamrocket.core.event.domain.exception.InvalidEventCapacityException;
import nl.teamrocket.core.event.domain.exception.InvalidEventStatusTransitionException;
import nl.teamrocket.core.event.domain.exception.InvalidEventTimeSlotException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * Vertaalt domeinexcepties naar HTTP-statuscodes (kwaliteitseis "Functionele
 * correctheid": correcte status-codes en response-structuur, documentatiedoc §"Kwaliteitseisen").
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(EventNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidEventStatusTransitionException.class)
    public ResponseEntity<ApiError> handleStatusTransition(InvalidEventStatusTransitionException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler({InvalidEventTimeSlotException.class, InvalidEventCapacityException.class,
            IllegalArgumentException.class})
    public ResponseEntity<ApiError> handleBadRequest(RuntimeException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> handleConcurrency(OptimisticLockingFailureException ex) {
        return build(HttpStatus.CONFLICT,
                "Het event is gewijzigd door iemand anders. Probeer opnieuw met de laatste versie.");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return build(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseEntity<ApiError> build(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ApiError(status.value(), message));
    }
}
