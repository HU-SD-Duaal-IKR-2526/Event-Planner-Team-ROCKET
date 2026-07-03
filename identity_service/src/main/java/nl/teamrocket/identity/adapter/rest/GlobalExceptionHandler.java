package nl.teamrocket.identity.adapter.rest;

import lombok.extern.slf4j.Slf4j;
import nl.teamrocket.identity.domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.stream.Collectors;

/**
 * Maps domain exceptions to RFC 9457 ProblemDetail responses.
 * The domain layer knows nothing about HTTP — this is purely adapter concern.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountAlreadyExistsException.class)
    public ProblemDetail handleAlreadyExists(AccountAlreadyExistsException ex) {
        return problem(HttpStatus.CONFLICT, "account-already-exists", ex.getMessage());
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ProblemDetail handleNotFound(AccountNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, "account-not-found", ex.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ProblemDetail handleBadCredentials(InvalidCredentialsException ex) {
        return problem(HttpStatus.UNAUTHORIZED, "invalid-credentials", ex.getMessage());
    }

    @ExceptionHandler(AccountLockedException.class)
    public ProblemDetail handleLocked(AccountLockedException ex) {
        return problem(HttpStatus.FORBIDDEN, "account-locked", ex.getMessage());
    }

    @ExceptionHandler(AccountNotVerifiedException.class)
    public ProblemDetail handleNotVerified(AccountNotVerifiedException ex) {
        return problem(HttpStatus.FORBIDDEN, "account-not-verified", ex.getMessage());
    }

    @ExceptionHandler(AccountAlreadyVerifiedException.class)
    public ProblemDetail handleAlreadyVerified(AccountAlreadyVerifiedException ex) {
        return problem(HttpStatus.CONFLICT, "account-already-verified", ex.getMessage());
    }

    @ExceptionHandler(AccountSuspendedException.class)
    public ProblemDetail handleSuspended(AccountSuspendedException ex) {
        return problem(HttpStatus.FORBIDDEN, "account-suspended", ex.getMessage());
    }

    @ExceptionHandler({
        VerificationTokenExpiredException.class,
        InvalidVerificationTokenException.class,
        NoVerificationTokenException.class
    })
    public ProblemDetail handleBadVerificationToken(RuntimeException ex) {
        return problem(HttpStatus.BAD_REQUEST, "invalid-verification-token", ex.getMessage());
    }

    @ExceptionHandler({
        InvalidResetTokenException.class,
        ResetTokenExpiredException.class
    })
    public ProblemDetail handleBadResetToken(RuntimeException ex) {
        return problem(HttpStatus.BAD_REQUEST, "invalid-reset-token", ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return problem(HttpStatus.BAD_REQUEST, "validation-failed", details);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);
        return problem(HttpStatus.INTERNAL_SERVER_ERROR, "internal-error",
                "An unexpected error occurred");
    }

    private ProblemDetail problem(HttpStatus status, String type, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(URI.create("https://eventplanner.nl/errors/" + type));
        return pd;
    }
}
